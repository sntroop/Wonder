package su.gwarden.music.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.Movie
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Switch
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import su.gwarden.music.R
import java.io.File

/**
 * Оформление приложения: акцентный цвет, фон (обои / GIF / видео),
 * размытие, прозрачность поверхностей и шрифт.
 *
 * Всё локально: SharedPreferences + файлы в filesDir/skin. Ничего не уходит
 * на сервер — настройки принадлежат устройству.
 */
object Skin {

    private const val PREFS = "wonder_skin"

    /** id встроенных шрифтов -> overlay-стиль темы. */
    private val FONT_STYLES = mapOf(
        "unbounded" to 0,
        "manrope" to R.style.Font_Overlay_Manrope,
        "inter" to R.style.Font_Overlay_Inter,
        "jetbrains" to R.style.Font_Overlay_Jetbrains,
        "ptserif" to R.style.Font_Overlay_PtSerif,
    )

    val FONT_TITLES = linkedMapOf(
        "unbounded" to "Unbounded",
        "manrope" to "Manrope",
        "inter" to "Inter",
        "jetbrains" to "JetBrains Mono",
        "ptserif" to "PT Serif",
    )

    var accent: Int = Color.WHITE
        private set
    var surfaceAlpha: Float = 1f
        private set
    var blur: Int = 0
        private set
    var dim: Float = 0.35f
        private set
    var fontId: String = "unbounded"
        private set
    var customFont: File? = null
        private set
    var bgType: String = "none"
        private set
    var bgFile: File? = null
        private set

    /** Человеческое имя выбранного фона для настроек. */
    fun bgTitle(): String = when (bgType) {
        "wallpaper" -> "Обои"
        "gif" -> "GIF"
        "video" -> "Видео"
        else -> "Нет"
    }

    fun fontTitle(): String =
        if (fontId == "custom") "Свой шрифт" else FONT_TITLES[fontId] ?: "Unbounded"

    fun load(ctx: Context) {
        val p = prefs(ctx)
        accent = p.getInt("accent", Color.WHITE)
        surfaceAlpha = p.getFloat("surface_alpha", 1f).coerceIn(0.25f, 1f)
        blur = p.getInt("blur", 0).coerceIn(0, 40)
        dim = p.getFloat("dim", 0.35f).coerceIn(0f, 0.85f)
        fontId = p.getString("font", "unbounded") ?: "unbounded"
        val customPath = p.getString("font_file", null)
        customFont = customPath?.let { File(it) }?.takeIf { it.exists() }
        if (fontId == "custom" && customFont == null) fontId = "unbounded"
        bgType = p.getString("bg_type", "none") ?: "none"
        val path = p.getString("bg_file", null)
        bgFile = path?.let { File(it) }?.takeIf { it.exists() }
        if (bgType != "none" && bgFile == null) bgType = "none"
    }

    fun save(ctx: Context, block: (android.content.SharedPreferences.Editor) -> Unit) {
        val e = prefs(ctx).edit()
        block(e)
        e.apply()
        load(ctx)
    }

    fun directory(ctx: Context): File =
        File(ctx.filesDir, "skin").apply { mkdirs() }

    /** Цвет текста/иконки ПОВЕРХ акцентной плашки. */
    fun onAccent(): Int {
        val lum = (0.299f * Color.red(accent) + 0.587f * Color.green(accent) + 0.114f * Color.blue(accent)) / 255f
        return if (lum > 0.6f) Color.BLACK else Color.WHITE
    }

    /**
     * Наложить шрифт на тему ДО создания вьюх.
     * Вызывается первым делом в onCreate каждой Activity.
     */
    fun applyTheme(activity: Activity) {
        load(activity)
        val overlay = FONT_STYLES[fontId] ?: 0
        if (overlay != 0) {
            runCatching { activity.theme.applyStyle(overlay, true) }
        }
    }

    /** То же для диалогов: их тема создаётся отдельно от темы Activity. */
    fun prepare(dialog: Dialog) {
        val overlay = if (fontId == "custom") 0 else FONT_STYLES[fontId] ?: 0
        if (overlay != 0) {
            runCatching { dialog.context.theme.applyStyle(overlay, true) }
        }
        runCatching {
            dialog.setOnShowListener {
                applyToDialog(dialog)
            }
        }
    }

    /** Догнать шрифтом уже показанный диалог (если свой onShow его перекрыл). */
    fun applyToDialog(dialog: Dialog) {
        val decor = dialog.window?.decorView ?: return
        applyFont(decor, dialog.context)
        watchFont(decor, dialog.context)
    }

    /** Полное применение оформления к уже созданному экрану. */
    fun apply(activity: Activity) {
        load(activity)
        installBackground(activity, activity.findViewById(android.R.id.content))
        activity.findViewById<View>(R.id.app_root)?.let { root ->
            // фон-слой уже нарисован BackgroundHost: не закрываем его цветом
            if (bgType == "none") root.setBackgroundResource(R.drawable.bg_app)
            else root.setBackgroundColor(Color.TRANSPARENT)
        }
        // Обои должны быть видны на ЛЮБОМ экране (шортсы, профиль, загрузка):
        // корни этих активити красят себя в чёрный/bg_app и закрывают слой.
        val content = activity.findViewById<ViewGroup>(android.R.id.content)
        if (bgType != "none" && content != null) {
            for (i in 0 until content.childCount) {
                val c = content.getChildAt(i)
                if (c.tag == "wonder_bg") continue
                c.setBackgroundColor(Color.TRANSPARENT)
            }
        }
        val decor = activity.window?.decorView
        applyAccent(decor)
        applyFont(decor, activity)
        watchSkin(decor, activity)
    }

    /**
     * Акцент — это не только ползунки. Красим:
     *  • белые «пилюли» (play, кнопки) → фон акцентом, содержимое контрастом;
     *  • векторные иконки навигации/действий плеера → акцентом;
     *  • мелкие ПРОПИСНЫЕ подписи-разделы (letterSpacing ≥ 0.05) → акцентом;
     *  • прогресс/свитчи/чипсы/каплю навигации.
     * Обложки, аватары и названия треков не трогаем.
     */
    fun applyAccent(root: View?) = applyAccent(root, false)

    private fun applyAccent(r: View?, inPill: Boolean) {
        val v = r ?: return
        val tagPill = v.tag == "accent_play" || v.tag == "accent_pill"
        val pill = inPill || tagPill || isWhitePill(v)

        when (v) {
            is ProgressStrip -> v.accent = accent
            is ClipProgress -> v.accent = accent
            is LiquidNav -> v.blobColor = accent
            is EqView -> v.accent = accent
            is SeekBar -> {
                v.progressTintList = ColorStateList.valueOf(accent)
                v.thumbTintList = ColorStateList.valueOf(accent)
            }
            is Switch -> {
                v.thumbTintList = ColorStateList.valueOf(accent)
                v.trackTintList = ColorStateList.valueOf(withAlpha(accent, 0x66))
            }
            is ProgressBar -> {
                v.progressTintList = ColorStateList.valueOf(accent)
                v.indeterminateTintList = ColorStateList.valueOf(accent)
            }
        }

        if (pill) {
            runCatching { v.backgroundTintList = ColorStateList.valueOf(accent) }
            when (v) {
                is android.widget.TextView -> v.setTextColor(onAccent())
                is ImageView -> v.setColorFilter(onAccent())
            }
        } else {
            if (v.id == R.id.chip_home || v.id == R.id.chip_search ||
                v.id == R.id.chip_my || v.id == R.id.chip_lib
            ) {
                v.backgroundTintList = ColorStateList.valueOf(accent)
            }
            if (v.id == R.id.mini_play || v.id == R.id.wave_play) {
                v.backgroundTintList = ColorStateList.valueOf(accent)
                if (v is android.widget.TextView) v.setTextColor(onAccent())
                if (v is ImageView) v.setColorFilter(accent)
            }
            if (v is ImageView && isRoleIcon(v)) {
                v.setColorFilter(accent)
            }
            if (v is android.widget.TextView) {
                val idName = runCatching { v.resources.getResourceEntryName(v.id) }.getOrNull()
                if (idName != null && idName.startsWith("now_") && idName.endsWith("_tx")) {
                    v.setTextColor(accent)
                }
                val s = v.text?.toString()?.trim().orEmpty()
                val small = v.textSize <= 13.5f * v.resources.displayMetrics.scaledDensity
                if (s.isNotEmpty() && small && v.letterSpacing >= 0.05f && s == s.uppercase()) {
                    v.setTextColor(accent)
                }
            }
        }

        // поверхности: прозрачность «стекла» (и возврат к 100%, когда двинули назад)
        val bgId = when (v.id) {
            R.id.mini -> R.drawable.bg_mini
            R.id.top_bar -> R.drawable.bg_topbar
            else -> 0
        }
        if (bgId != 0) {
            runCatching {
                val d = v.background
                if (d != null) {
                    d.mutate().alpha = (255 * surfaceAlpha).toInt()
                    v.background = d
                }
            }
        }
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) applyAccent(v.getChildAt(i), pill)
        }
    }

    /** Белая сплошная «пилюля» (bg_circle_white, bg_btn и подобные). */
    private fun isWhitePill(v: View): Boolean {
        val d = v.background ?: return false
        if (d !is GradientDrawable) return false
        val c = runCatching { d.color?.defaultColor }.getOrNull() ?: return false
        if (Color.alpha(c) < 240) return false
        val lum = (0.299f * Color.red(c) + 0.587f * Color.green(c) + 0.114f * Color.blue(c)) / 255f
        return lum > 0.82f
    }

    /** Иконки навигации/плеера/мини-плеера (но не обложки и не аватары). */
    private fun isRoleIcon(v: ImageView): Boolean {
        val d = v.drawable ?: return false
        if (d is android.graphics.drawable.BitmapDrawable) return false
        val name = runCatching { v.resources.getResourceEntryName(v.id) }.getOrNull() ?: return false
        if (name.contains("cover") || name.contains("avatar") || name.contains("art")) return false
        // nav_* красит tintNavIcons (активная — контрастом на капле),
        // иначе watcher перебивал бы её цвет на каждом layout
        return name.startsWith("now_") || name.startsWith("mini_") ||
            name.startsWith("wave_")
    }

    /* ── шрифт ──────────────────────────────────────────────────────────
       Тема задаёт шрифт только НОВЫМ вьюхам, а приложение половину UI
       строит кодом и местами жёстко ставит Unbounded. Поэтому выбранный
       шрифт дополнительно раздаётся обходом дерева, а за новыми вьюхами
       следит OnGlobalLayout (пересборка настроек, списки, шторки). */

    private var baseTf: Typeface? = null
    private var baseKey = ""
    private val styleTf = arrayOfNulls<Typeface>(4)
    private var lastWalkAt = 0L
    private val watchedRoots =
        java.util.Collections.newSetFromMap(java.util.WeakHashMap<View, Boolean>())

    /** Шрифт приложения для текущего выбора (Unbounded, если не выбран другой). */
    fun typeface(ctx: Context, bold: Boolean = false): Typeface? =
        typefaceFor(ctx, if (bold) Typeface.BOLD else Typeface.NORMAL)

    fun typefaceFor(ctx: Context, style: Int): Typeface? {
        val base = baseTypeface(ctx) ?: return null
        val s = style and (Typeface.BOLD or Typeface.ITALIC)
        val idx = (if (s and Typeface.BOLD != 0) 1 else 0) +
            (if (s and Typeface.ITALIC != 0) 2 else 0)
        styleTf[idx]?.let { return it }
        val tf = runCatching { Typeface.create(base, s) }.getOrNull() ?: base
        styleTf[idx] = tf
        return tf
    }

    private fun baseTypeface(ctx: Context): Typeface? {
        val key = fontId + "|" + (customFont?.absolutePath ?: "")
        if (key == baseKey && baseTf != null) return baseTf
        baseTf = when (fontId) {
            "manrope" -> ResourcesCompat.getFont(ctx, R.font.manrope)
            "inter" -> ResourcesCompat.getFont(ctx, R.font.inter)
            "jetbrains" -> ResourcesCompat.getFont(ctx, R.font.jetbrains_mono)
            "ptserif" -> ResourcesCompat.getFont(ctx, R.font.pt_serif)
            "custom" -> customFont?.let {
                runCatching { Typeface.createFromFile(it) }.getOrNull()
            }
            else -> ResourcesCompat.getFont(ctx, R.font.unbounded_family)
        }
        baseKey = key
        java.util.Arrays.fill(styleTf, null)
        return baseTf
    }

    /** Раздать шрифт всем TextView в дереве (включая программные). */
    fun applyFont(root: View?, ctx: Context) {
        val r = root ?: return
        if (r is android.widget.TextView) {
            val want = typefaceFor(ctx, r.typeface?.style ?: Typeface.NORMAL)
            if (want != null && r.typeface !== want) r.typeface = want
        }
        if (r is ViewGroup) {
            for (i in 0 until r.childCount) applyFont(r.getChildAt(i), ctx)
            if (r is RecyclerView) watchRecycler(r, ctx)
        }
    }

    /**
     * Следить за пересборкой UI: каждый global layout (добавили вьюхи,
     * сменили экран) аккуратно догоняем шрифт. Не чаще 2 раз в секунду.
     */
    fun watchFont(root: View?, ctx: Context) = watchSkin(root, ctx)

    /**
     * Следить за пересборкой UI: главная/настройки/списки создают вьюхи
     * ПОСЛЕ onResume, поэтому и акцент, и шрифт нужно догонять на каждом
     * global layout (не чаще ~3 раз в секунду).
     */
    fun watchSkin(root: View?, ctx: Context) {
        val r = root ?: return
        if (!watchedRoots.add(r)) return
        r.viewTreeObserver.addOnGlobalLayoutListener {
            val now = android.os.SystemClock.uptimeMillis()
            if (now - lastWalkAt < 350) return@addOnGlobalLayoutListener
            lastWalkAt = now
            applyAccent(r)
            if (fontId != "unbounded" || customFont != null) applyFont(r, ctx)
        }
    }

    /** Совместимость со старыми вызовами. */
    fun applyCustomFont(root: View?) {
        val ctx = root?.context ?: return
        applyFont(root, ctx)
    }

    /** Новые элементы списков (переиспользованные) тоже должны получить шрифт. */
    private fun watchRecycler(rv: RecyclerView, ctx: Context) {
        if (rv.getTag(R.id.skin_font_watch) != null) return
        rv.setTag(R.id.skin_font_watch, true)
        rv.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                applyFont(view, ctx)
            }

            override fun onChildViewDetachedFromWindow(view: View) = Unit
        })
    }

    /**
     * Фон приложения: картинка / GIF / видео позади всего интерфейса.
     * Слой вставляется первым ребёнком content-фрейма, поэтому не мешает
     * ни спискам, ни шторкам, ни анимациям.
     */
    fun installBackground(activity: Activity, content: View?) {
        val frame = content as? FrameLayout ?: return
        val existing = frame.findViewWithTag<BackgroundHost>("wonder_bg")
        if (bgType == "none" || bgFile == null) {
            existing?.let { frame.removeView(it) }
            return
        }
        val host = existing ?: BackgroundHost(activity).also {
            it.tag = "wonder_bg"
            frame.addView(it, 0, FrameLayout.LayoutParams(-1, -1))
        }
        host.configure(bgType, bgFile!!, blur, dim)
    }

    /** Фон для диалогов больше не дублируется: окно прозрачное, за ним виден
     *  фон Activity. Второй BackgroundHost в окне плеера на видео/GIF давал
     *  чёрный экран (второй декодер/поверхность). Оставлено для совместимости. */
    @Suppress("UNUSED_PARAMETER")
    fun installDialogBackground(dialog: Dialog) = Unit

    fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    /** Пауза фонового видео/GIF, когда приложение уходит в фон. */
    fun pauseBackground(activity: Activity) {
        (activity.findViewById<View>(android.R.id.content) as? FrameLayout)
            ?.findViewWithTag<BackgroundHost>("wonder_bg")
            ?.pausePlayback()
    }

    fun resumeBackground(activity: Activity) {
        (activity.findViewById<View>(android.R.id.content) as? FrameLayout)
            ?.findViewWithTag<BackgroundHost>("wonder_bg")
            ?.resumePlayback()
    }

    fun isBgActive(): Boolean = bgType != "none" && bgFile != null

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}

/**
 * Фоновый слой: картинка (с размытием), GIF или видео.
 * Живёт под интерфейсом, сам масштабируется «под заливку» экрана.
 */
class BackgroundHost(context: Context) : FrameLayout(context) {

    private var image: ImageView? = null
    private var gif: GifLayer? = null
    private var videoPlayer: androidx.media3.exoplayer.ExoPlayer? = null
    private var texture: android.view.TextureView? = null
    private var scrim: View? = null
    private var currentKey = ""

    fun configure(type: String, file: File, blur: Int, dim: Float) {
        val key = "$type|${file.absolutePath}|${file.length()}|$blur"
        if (key != currentKey) {
            currentKey = key
            releaseVideo()
            removeAllViews()
            image = null
            gif = null

            when (type) {
                "video" -> {
                    // TextureView, а не VideoView: SurfaceView нельзя размыть,
                    // а TextureView умеет RenderEffect на Android 12+.
                    val tv = android.view.TextureView(context)
                    addView(tv, LayoutParams(-1, -1))
                    runCatching {
                        val player = androidx.media3.exoplayer.ExoPlayer.Builder(context).build()
                        player.setMediaItem(
                            androidx.media3.common.MediaItem.fromUri(
                                android.net.Uri.fromFile(file)))
                        player.repeatMode = androidx.media3.common.Player.REPEAT_MODE_ONE
                        player.volume = 0f
                        player.setVideoTextureView(tv)
                        player.prepare()
                        player.playWhenReady = true
                        videoPlayer = player
                        texture = tv
                    }
                    applyBlur(tv, blur)
                }
                "gif" -> {
                    val g = GifLayer(context)
                    g.layoutParams = LayoutParams(-1, -1)
                    addView(g)
                    g.setSource(file)
                    gif = g
                    applyBlur(g, blur)
                }
                else -> {
                    val iv = ImageView(context)
                    iv.layoutParams = LayoutParams(-1, -1)
                    iv.scaleType = ImageView.ScaleType.CENTER_CROP
                    addView(iv)
                    val bmp = decodeWallpaper(file, blur)
                    if (bmp != null) iv.setImageBitmap(bmp)
                    image = iv
                }
            }

            val s = View(context)
            s.setBackgroundColor(android.graphics.Color.argb((dim * 255).toInt(), 0, 0, 0))
            addView(s, LayoutParams(-1, -1))
            scrim = s
        } else {
            // затемнение меняется ползунком — пересобирать слой (и заново
            // размывать обои) на каждый пиксель нельзя
            scrim?.setBackgroundColor(android.graphics.Color.argb((dim * 255).toInt(), 0, 0, 0))
        }
    }

    /** GIF/видео размываются аппаратно, начиная с Android 12. Обои — всегда. */
    private fun applyBlur(view: View, radius: Int) {
        if (radius <= 0 || Build.VERSION.SDK_INT < 31) return
        runCatching {
            view.setRenderEffect(
                android.graphics.RenderEffect.createBlurEffect(
                    radius.toFloat(), radius.toFloat(),
                    android.graphics.Shader.TileMode.CLAMP))
        }
    }

    private fun releaseVideo() {
        runCatching { videoPlayer?.release() }
        videoPlayer = null
        texture = null
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        videoPlayer?.playWhenReady = visibility == VISIBLE
    }

    fun pausePlayback() {
        videoPlayer?.playWhenReady = false
        gif?.stop()
    }

    fun resumePlayback() {
        videoPlayer?.playWhenReady = true
    }

    override fun onDetachedFromWindow() {
        releaseVideo()
        gif?.stop()
        super.onDetachedFromWindow()
    }

    private fun decodeWallpaper(file: File, blurRadius: Int): Bitmap? {
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            var sample = 1
            while (bounds.outWidth / sample > 1600) sample *= 2
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            var bmp = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
            if (blurRadius > 0) bmp = Blur.bitmap(bmp, blurRadius)
            bmp
        }.getOrNull()
    }
}

/** Размытие без RenderScript: уменьшение + три прохода коробки + масштаб. */
object Blur {
    fun bitmap(src: Bitmap, radius: Int): Bitmap {
        if (radius <= 0) return src
        val scale = (1f / (1f + radius / 8f)).coerceAtLeast(0.12f)
        val small = Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt().coerceAtLeast(4),
            (src.height * scale).toInt().coerceAtLeast(4),
            true,
        )
        val w = small.width
        val h = small.height
        val px = IntArray(w * h)
        small.getPixels(px, 0, w, 0, 0, w, h)
        val r = (radius / 4).coerceIn(1, 12)
        boxBlur(px, w, h, r)
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(px, 0, w, 0, 0, w, h)
        return Bitmap.createScaledBitmap(out, src.width, src.height, true)
    }

    private fun boxBlur(px: IntArray, w: Int, h: Int, radius: Int) {
        val tmp = IntArray(px.size)
        for (pass in 0 until 2) {
            for (y in 0 until h) {
                var r = 0; var g = 0; var b = 0; var n = 0
                for (x in -radius..radius) {
                    val xx = x.coerceIn(0, w - 1)
                    val c = px[y * w + xx]
                    r += (c shr 16) and 0xFF; g += (c shr 8) and 0xFF; b += c and 0xFF; n++
                }
                for (x in 0 until w) {
                    tmp[y * w + x] = (0xFF shl 24) or ((r / n) shl 16) or ((g / n) shl 8) or (b / n)
                    val xOut = (x - radius).coerceIn(0, w - 1)
                    val xIn = (x + radius + 1).coerceIn(0, w - 1)
                    val cOut = px[y * w + xOut]
                    val cIn = px[y * w + xIn]
                    r += ((cIn shr 16) and 0xFF) - ((cOut shr 16) and 0xFF)
                    g += ((cIn shr 8) and 0xFF) - ((cOut shr 8) and 0xFF)
                    b += (cIn and 0xFF) - (cOut and 0xFF)
                }
            }
            for (x in 0 until w) {
                var r = 0; var g = 0; var b = 0; var n = 0
                for (y in -radius..radius) {
                    val yy = y.coerceIn(0, h - 1)
                    val c = tmp[yy * w + x]
                    r += (c shr 16) and 0xFF; g += (c shr 8) and 0xFF; b += c and 0xFF; n++
                }
                for (y in 0 until h) {
                    px[y * w + x] = (0xFF shl 24) or ((r / n) shl 16) or ((g / n) shl 8) or (b / n)
                    val yOut = (y - radius).coerceIn(0, h - 1)
                    val yIn = (y + radius + 1).coerceIn(0, h - 1)
                    val cOut = tmp[yOut * w + x]
                    val cIn = tmp[yIn * w + x]
                    r += ((cIn shr 16) and 0xFF) - ((cOut shr 16) and 0xFF)
                    g += ((cIn shr 8) and 0xFF) - ((cOut shr 8) and 0xFF)
                    b += (cIn and 0xFF) - (cOut and 0xFF)
                }
            }
        }
    }
}

/** GIF-слой: AnimatedImageDrawable на Android 9+, Movie на старых. */
class GifLayer(context: Context) : View(context) {

    private var drawable: android.graphics.drawable.Drawable? = null
    private var movie: Movie? = null
    private var movieStart = 0L
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    fun setSource(file: File) {
        if (Build.VERSION.SDK_INT >= 28) {
            runCatching {
                val src = android.graphics.ImageDecoder.createSource(file)
                val d = android.graphics.ImageDecoder.decodeDrawable(src)
                drawable = d
                (d as? android.graphics.drawable.Animatable)?.start()
                invalidate()
            }
        } else {
            runCatching {
                movie = Movie.decodeFile(file.absolutePath)
                movieStart = android.os.SystemClock.uptimeMillis()
                invalidate()
            }
        }
    }

    fun stop() {
        (drawable as? android.graphics.drawable.Animatable)?.stop()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = drawable
        if (d != null) {
            val srcW = d.intrinsicWidth.coerceAtLeast(1)
            val srcH = d.intrinsicHeight.coerceAtLeast(1)
            val scale = maxOf(width.toFloat() / srcW, height.toFloat() / srcH)
            val dw = (srcW * scale).toInt()
            val dh = (srcH * scale).toInt()
            d.setBounds((width - dw) / 2, (height - dh) / 2, (width + dw) / 2, (height + dh) / 2)
            d.draw(canvas)
            postInvalidateOnAnimation()
            return
        }
        val m = movie ?: return
        val dur = m.duration().coerceAtLeast(1)
        val t = ((android.os.SystemClock.uptimeMillis() - movieStart) % dur).toInt()
        m.setTime(t)
        val scale = maxOf(width.toFloat() / m.width(), height.toFloat() / m.height())
        val save = canvas.save()
        canvas.translate((width - m.width() * scale) / 2f, (height - m.height() * scale) / 2f)
        canvas.scale(scale, scale)
        m.draw(canvas, 0f, 0f, paint)
        canvas.restoreToCount(save)
        invalidate()
    }
}
