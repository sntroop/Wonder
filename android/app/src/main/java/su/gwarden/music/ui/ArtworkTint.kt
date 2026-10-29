package su.gwarden.music.ui

import android.graphics.Bitmap
import su.gwarden.music.api.ApiClient
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Цвет приложения из обложки играющего трека.
 *
 * Взяли трек - достаём из его обложки самый «живой» цвет и рассылаем всем,
 * кто хочет перекраситься (фон приложения, нижний бар, мини-плеер).
 * Сам по себе цвет неяркий: в фон он идёт затемнённым, чтобы белый текст
 * оставался читаемым.
 */
object ArtworkTint {

    interface Listener {
        fun onColor(color: Int)
    }

    private val listeners = CopyOnWriteArrayList<Listener>()
    private var lastUrl: String? = null
    private var lastColor = 0

    val color: Int get() = lastColor

    fun addListener(l: Listener) {
        if (!listeners.contains(l)) listeners.add(l)
        if (lastColor != 0) l.onColor(lastColor)
    }

    fun removeListener(l: Listener) {
        listeners.remove(l)
    }

    /** Загружает обложку по ссылке и рассылает её цвет (кеш - мгновенно). */
    fun applyUrl(url: String?) {
        if (url.isNullOrEmpty()) return
        if (url == lastUrl) return
        lastUrl = url
        ApiClient.log("tint", "req " + url.takeLast(40))
        ImageLoader.bitmap(url, 128) { bmp ->
            val c = fromBitmap(bmp)
            if (c == null) {
                // чёрно-белая/серая обложка: никакого «цвета из прошлого трека»,
                // явно возвращаем нейтральный монохром
                ApiClient.log("tint", "нет цвета (чб) - нейтральный фон")
                emit(0)
                return@bitmap
            }
            ApiClient.log("tint", "цвет #" + Integer.toHexString(c) + " слушателей=" + listeners.size)
            emit(c)
        }
    }

    /** Рассылает цвет слушателям. 0 = «цвета нет» (чб обложка). */
    private fun emit(c: Int) {
        if (c == lastColor) return
        lastColor = c
        for (l in listeners) {
            try {
                l.onColor(c)
            } catch (e: Throwable) {
                ApiClient.log("tint", "слушатель упал: " + e)
            }
        }
    }

    /** Доминирующий насыщенный цвет обложки (или null, если картинка серая). */
    fun fromBitmap(src: Bitmap?): Int? {
        if (src == null) return null
        val small = runCatching { Bitmap.createScaledBitmap(src, 32, 32, true) }
            .getOrNull() ?: return null
        val buckets = HashMap<Int, FloatArray>()
        for (y in 0 until small.height) {
            for (x in 0 until small.width) {
                val c = small.getPixel(x, y)
                if (((c ushr 24) and 0xFF) < 200) continue
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                val mx = maxOf(r, g, b)
                val mn = minOf(r, g, b)
                if (mx < 26 || mn > 235) continue
                val sat = if (mx == 0) 0f else (mx - mn).toFloat() / mx
                if (sat < 0.10f) continue
                val key = ((r / 64) shl 6) or ((g / 64) shl 3) or (b / 64)
                val arr = buckets.getOrPut(key) { FloatArray(6) }
                val w = 0.25f + sat * sat
                arr[0] += w
                arr[1] += r * w
                arr[2] += g * w
                arr[3] += b * w
                if (sat > arr[4]) arr[4] = sat
                arr[5] += 1f
            }
        }
        val best = buckets.values.maxByOrNull { it[0] * (0.65f + it[4]) } ?: return null
        // цветной пиксель должен занимать заметную часть обложки: мелкий
        // цветной логотип на чб картинке не должен красить всё приложение
        val share = best[5] / (small.width * small.height).toFloat()
        if (share < 0.06f) return null
        val n = best[0].coerceAtLeast(0.001f)
        val raw = ((best[1] / n).toInt() shl 16) or
            ((best[2] / n).toInt() shl 8) or (best[3] / n).toInt()
        return boost(raw)
    }

    /**
     * Цвет обложки для фона: средние пиксели часто тёмные и мутные, поэтому
     * подтягиваем яркость и насыщенность в HSV - иначе на экране почти чёрно.
     */
    private fun boost(c: Int): Int {
        val hsv = FloatArray(3)
        android.graphics.Color.RGBToHSV(
            (c shr 16) and 0xFF, (c shr 8) and 0xFF, c and 0xFF, hsv,
        )
        if (hsv[1] < 0.55f) hsv[1] = 0.55f
        if (hsv[2] < 0.85f) hsv[2] = 0.85f
        val out = android.graphics.Color.HSVToColor(hsv)
        return out and 0x00FFFFFF
    }
}
