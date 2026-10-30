package su.gwarden.music.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.LruCache
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import su.gwarden.music.api.ApiClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.io.File

/**
 * Свой загрузчик обложек вместо Glide: у Glide жёсткий таймаут 2.5с, из-за
 * которого картинки через наш прокси (/api/img) не успевали и карточки
 * оставались пустыми. Здесь: 3 попытки, человеческие таймауты, память-кэш,
 * скругление углов и отчёт об ошибках на сервер (/api/log), чтобы видеть
 * причину без adb.
 */
object ImageLoader {

    private const val TAG = "ImageLoader"

    @Volatile
    private var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var diskReady = false

    /** Дисковый кэш обложек: повторные показы - мгновенно, без сети. */
    fun init(ctx: Context) {
        if (diskReady) return
        diskReady = true
        runCatching {
            client = OkHttpClient.Builder()
                .cache(okhttp3.Cache(File(ctx.cacheDir, "imgcache"), 32L * 1024 * 1024))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
        }
    }

    private val pool = Executors.newFixedThreadPool(4)
    private val main = Handler(Looper.getMainLooper())
    private val reported = AtomicInteger(0)
    private val okCount = AtomicInteger(0)
    private val skipCount = AtomicInteger(0)
    private val loggedCount = AtomicInteger(0)

    // кэш в килобайтах
    private val cache = object : LruCache<String, Bitmap>(24 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    fun load(
        view: ImageView,
        url: String?,
        roundedDp: Int = 0,
        placeholderRes: Int = 0,
        placeholderBmp: Bitmap? = null,
    ) {
        val tag = "${url.orEmpty()}|$roundedDp"
        if (view.tag != tag) {
            view.tag = tag
            view.animate().cancel()
            view.alpha = 1f
            when {
                placeholderBmp != null -> view.setImageBitmap(placeholderBmp)
                placeholderRes != 0 -> view.setImageResource(placeholderRes)
                else -> view.setImageDrawable(null)
            }
        }
        if (url.isNullOrEmpty()) return

        cache.get(url)?.let { bmp ->
            view.animate().cancel()
            view.alpha = 1f
            if (roundedDp > 0) view.setImageDrawable(decorate(view, bmp, roundedDp))
            else view.setImageBitmap(bmp)
            return
        }

        pool.execute {
            val bmp = fetch(url, view)
            main.post {
                if (view.tag == tag && bmp != null) {
                    cache.put(url, bmp)
                    // БЕЗ BitmapDrawable: он пересчитывает плотность и картинка
                    // может уехать/не показаться. setImageBitmap работает везде.
                    // плавное проявление вместо «щёлк и появилось»
                    view.animate().cancel()
                    view.alpha = 0f
                    if (roundedDp > 0) {
                        view.setImageDrawable(decorate(view, bmp, roundedDp))
                    } else {
                        view.setImageBitmap(bmp)
                    }
                    view.animate().alpha(1f).setDuration(220).start()
                    if (okCount.incrementAndGet() <= 30) {
                        ApiClient.log("img_ok", "${bmp.width}x${bmp.height} w=${view.width} ${url.takeLast(34)}")
                    }
                } else {
                    if (skipCount.incrementAndGet() <= 15) {
                        ApiClient.log("img_skip", "tagEqual=${view.tag == tag} bmp=${bmp != null} w=${view.width}")
                    }
                }
            }
        }
    }

    // кэш размытых обложек (фон карточки плеера)
    private val blurCache = object : LruCache<String, Bitmap>(16 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int =
            (value.byteCount / 1024).coerceAtLeast(1)
    }

    /**
     * Сильно размытая и затемнённая обложка - фон полноэкранного плеера.
     * Размываем на маленьком битмапе (110px) и отдаём как есть: ImageView/
     * RoundedBitmapDrawable растянет его с фильтрацией -> гладкое размытие
     * без RenderScript (он выпилен в новых Android).
     */
    fun blurred(url: String?, onReady: (Bitmap?) -> Unit) {
        if (url.isNullOrEmpty()) {
            onReady(null)
            return
        }
        blurCache.get(url)?.let {
            onReady(it)
            return
        }
        pool.execute {
            val src = fetchRaw(url, 200)
            val out = src?.let { makeBlurBg(it) }
            if (out != null) blurCache.put(url, out)
            main.post { onReady(out) }
        }
    }

    private fun makeBlurBg(src: Bitmap): Bitmap {
        val w = 110
        val h = (w * src.height.toFloat() / src.width.toFloat()).toInt().coerceIn(40, 300)
        val small = Bitmap.createScaledBitmap(src, w, h, true)
        val pix = IntArray(w * h)
        small.getPixels(pix, 0, w, 0, 0, w, h)
        boxBlur(pix, w, h, 10)
        boxBlur(pix, w, h, 6)
        // затемняем и кладём вертикальный градиент: у краёв темнее, в середине
        // обложка «дышит», чтобы белый текст читался
        for (y in 0 until h) {
            val t = y.toFloat() / (h - 1).coerceAtLeast(1)
            val dark = if (t < 0.5f) {
                0.42f + (0.08f - 0.42f) * (t / 0.5f)
            } else {
                0.08f + (0.52f - 0.08f) * ((t - 0.5f) / 0.5f)
            }
            val k = 0.48f * (1f - dark)
            for (x in 0 until w) {
                val i = y * w + x
                val c = pix[i]
                val a = (c ushr 24) and 0xFF
                val r = ((c shr 16 and 0xFF) * k).toInt().coerceIn(0, 255)
                val g = ((c shr 8 and 0xFF) * k).toInt().coerceIn(0, 255)
                val b = ((c and 0xFF) * k).toInt().coerceIn(0, 255)
                pix[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(pix, 0, w, 0, 0, w, h)
        return out
    }

    /** Размытие коробкой: два прохода по горизонтали/вертикали - как гаусс. */
    private fun boxBlur(pix: IntArray, w: Int, h: Int, radius: Int) {
        if (radius < 1) return
        val tmp = IntArray(pix.size)
        for (y in 0 until h) {
            val row = y * w
            for (x in 0 until w) {
                var a = 0
                var r = 0
                var g = 0
                var b = 0
                var n = 0
                val from = (x - radius).coerceAtLeast(0)
                val to = (x + radius).coerceAtMost(w - 1)
                for (i in from..to) {
                    val c = pix[row + i]
                    a += (c ushr 24) and 0xFF
                    r += (c shr 16) and 0xFF
                    g += (c shr 8) and 0xFF
                    b += c and 0xFF
                    n++
                }
                tmp[row + x] = ((a / n) shl 24) or ((r / n) shl 16) or
                    ((g / n) shl 8) or (b / n)
            }
        }
        for (x in 0 until w) {
            for (y in 0 until h) {
                var a = 0
                var r = 0
                var g = 0
                var b = 0
                var n = 0
                val from = (y - radius).coerceAtLeast(0)
                val to = (y + radius).coerceAtMost(h - 1)
                for (i in from..to) {
                    val c = tmp[i * w + x]
                    a += (c ushr 24) and 0xFF
                    r += (c shr 16) and 0xFF
                    g += (c shr 8) and 0xFF
                    b += c and 0xFF
                    n++
                }
                pix[y * w + x] = ((a / n) shl 24) or ((r / n) shl 16) or
                    ((g / n) shl 8) or (b / n)
            }
        }
    }

    /** Загрузить просто битмап (для уведомления). */
    fun bitmap(url: String?, targetPx: Int = 512, onReady: (Bitmap?) -> Unit) {
        if (url.isNullOrEmpty()) {
            onReady(null)
            return
        }
        cache.get(url)?.let {
            onReady(it)
            return
        }
        pool.execute {
            val bmp = fetchRaw(url, targetPx)
            if (bmp != null) cache.put(url, bmp)
            main.post { onReady(bmp) }
        }
    }

    private fun decorate(view: ImageView, bmp: Bitmap, roundedDp: Int) =
        if (roundedDp > 0) {
            val px = (roundedDp * view.resources.displayMetrics.density).toInt()
            RoundedBitmapDrawableFactory.create(view.resources, bmp).apply {
                cornerRadius = px.toFloat()
            }
        } else {
            BitmapDrawable(view.resources, bmp)
        }

    private fun fetch(url: String, view: ImageView): Bitmap? {
        val target = view.width.takeIf { it > 0 }
            ?: view.layoutParams?.width?.takeIf { it > 0 }
            ?: 420
        return fetchRaw(url, target)
    }

    private fun fetchRaw(url: String, target: Int): Bitmap? {
        var lastErr = "unknown"
        for (attempt in 0 until 3) {
            try {
                val req = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Wonder/1.0")
                    .build()
                client.newCall(req).execute().use { r ->
                    if (!r.isSuccessful) {
                        lastErr = "HTTP ${r.code}"
                    } else {
                        val bytes = r.body?.bytes()
                        if (bytes == null || bytes.isEmpty()) {
                            lastErr = "empty body"
                        } else {
                            val bmp = decode(bytes, target)
                            if (bmp != null) return bmp
                            lastErr = "decode failed, ${bytes.size} bytes"
                        }
                    }
                }
            } catch (e: Exception) {
                lastErr = "${e.javaClass.simpleName}: ${e.message}"
            }
            try {
                Thread.sleep(250L * (attempt + 1))
            } catch (_: InterruptedException) {
                break
            }
        }
        Log.w(TAG, "load failed: $lastErr ($url)")
        report(url, lastErr)
        return null
    }

    /** Отчёт об ошибке на сервер - чтобы разбираться без logcat. */
    private fun report(url: String, err: String) {
        if (reported.incrementAndGet() > 15) return
        runCatching {
            val u = url.replace("\\", "").replace("\"", "").take(600)
            val e = err.replace("\"", "'").take(200)
            val body = """{"url":"$u","err":"$e"}"""
            val req = Request.Builder()
                .url(ApiClient.baseUrl + "/api/log")
                .post(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()
            client.newCall(req).execute().close()
        }
    }

    private fun decode(bytes: ByteArray, target: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= target &&
            bounds.outHeight / (sample * 2) >= target
        ) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
    }
}
