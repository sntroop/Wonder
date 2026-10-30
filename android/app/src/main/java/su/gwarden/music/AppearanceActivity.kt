package su.gwarden.music

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import su.gwarden.music.ui.Skin
import java.io.File
import kotlin.math.roundToInt

/**
 * «Внешний вид»: акцентный цвет, фон (обои / GIF / видео), размытие,
 * прозрачность поверхностей и шрифт — включая свой .ttf из памяти.
 *
 * Всё применяется сразу и хранится только на телефоне.
 */
class AppearanceActivity : Activity() {

    private val pickWallpaper = 101
    private val pickGif = 102
    private val pickVideo = 103
    private val pickFont = 104

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).roundToInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        su.gwarden.music.ui.Skin.applyTheme(this)
        super.onCreate(savedInstanceState)
        build()
    }

    override fun onResume() {
        super.onResume()
        Skin.apply(this)
        if (Skin.fontId == "custom") {
            window?.decorView?.let { Skin.applyCustomFont(it) }
        }
    }

    // ── экран ────────────────────────────────────────────────────────

    private fun build() {
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(0xFF070708.toInt())
        scroll.fitsSystemWindows = true

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(18), dp(18), dp(18), dp(30))
        scroll.addView(root, ViewGroup.LayoutParams(-1, -2))

        // шапка
        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        head.setPadding(0, dp(6), 0, dp(8))

        val back = TextView(this)
        back.text = "‹"
        back.setTextColor(Color.WHITE)
        back.setTextSize(30f)
        back.setPadding(dp(8), 0, dp(14), dp(4))
        back.setOnClickListener { finish() }
        head.addView(back)

        val title = TextView(this)
        title.text = "Внешний вид"
        title.setTextColor(Color.WHITE)
        title.setTextSize(23f)
        title.setTypeface(title.typeface, Typeface.BOLD)
        head.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(head)

        // ── акцент ───────────────────────────────────────────────────
        root.addView(section("АКЦЕНТ"))
        root.addView(hint("Кнопки, ползунки, капля навигации и переключатели."))

        val swatches = HorizontalScrollView(this)
        swatches.isHorizontalScrollBarEnabled = false
        val swRow = LinearLayout(this)
        swRow.orientation = LinearLayout.HORIZONTAL
        swRow.setPadding(0, dp(12), 0, dp(4))
        swatches.addView(swRow)

        val colors = intArrayOf(
            0xFFFFFFFF.toInt(), 0xFFFF3B30.toInt(), 0xFFFF8A28.toInt(), 0xFFE8D44D.toInt(),
            0xFF8DF05C.toInt(), 0xFF2DD4BF.toInt(), 0xFF3ABEFF.toInt(), 0xFF7C5CFF.toInt(),
            0xFFFF4FA3.toInt(),
        )
        for (c in colors) swRow.addView(swatch(c))
        root.addView(swatches)

        val custom = rowButton("Свой цвет…", "выбрать RGB") { showColorPicker() }
        root.addView(custom)

        // ── фон ──────────────────────────────────────────────────────
        root.addView(section("ФОН"))
        root.addView(hint("Обои, GIF или видео за всем интерфейсом и в плеере."))

        val bgRow = LinearLayout(this)
        bgRow.orientation = LinearLayout.HORIZONTAL
        bgRow.setPadding(0, dp(10), 0, 0)
        bgRow.addView(bgButton("Обои") { pick(pickWallpaper, "image/*") })
        bgRow.addView(bgButton("GIF") { pick(pickGif, "image/gif") })
        bgRow.addView(bgButton("Видео") { pick(pickVideo, "video/*") })
        bgRow.addView(bgButton("Убрать") {
            Skin.save(this) { e -> e.putString("bg_type", "none") }
            Skin.apply(this)
            toast("Фон убран")
        })
        root.addView(bgRow)

        val current = TextView(this)
        current.text = "Сейчас: ${Skin.bgTitle()}"
        current.setTextColor(0xFF8A8A8A.toInt())
        current.setTextSize(12f)
        current.setPadding(0, dp(8), 0, 0)
        root.addView(current)

        root.addView(sliderRow("Размытие", Skin.blur.toFloat(), 0f, 40f, 1f,
            { "${it.roundToInt()} px" }, live = false) { v ->
            Skin.save(this) { e -> e.putInt("blur", v.roundToInt()) }
            Skin.apply(this)
        })
        root.addView(hint(when {
            Skin.bgType == "none" -> "Сначала выберите обои, GIF или видео — размывать пока нечего."
            Skin.bgType == "video" && android.os.Build.VERSION.SDK_INT < 31 ->
                "На этом Android видео не размывается — работает только затемнение."
            Skin.bgType == "gif" && android.os.Build.VERSION.SDK_INT < 31 ->
                "На этом Android GIF не размывается — работает только затемнение."
            else -> "Применяется после отпускания ползунка. Обои — всегда, GIF/видео — на Android 12+."
        }))

        root.addView(sliderRow("Затемнение", Skin.dim, 0f, 0.85f, 0.01f,
            { "${(it * 100).roundToInt()}%" }) { v ->
            Skin.save(this) { e -> e.putFloat("dim", v) }
            Skin.apply(this)
        })

        // ── поверхности ──────────────────────────────────────────────
        root.addView(section("ПОВЕРХНОСТИ"))
        root.addView(hint("Прозрачность стеклянных панелей: мини-плеер, шапка, карточки."))
        root.addView(sliderRow("Прозрачность", Skin.surfaceAlpha, 0.25f, 1f, 0.01f,
            { "${(it * 100).roundToInt()}%" }) { v ->
            Skin.save(this) { e -> e.putFloat("surface_alpha", v) }
            Skin.apply(this)
        })

        // ── шрифт ────────────────────────────────────────────────────
        root.addView(section("ШРИФТ"))
        root.addView(hint("Выбор применяется ко всему приложению, включая шторки."))

        for ((id, name) in Skin.FONT_TITLES) {
            root.addView(fontRow(id, name, null))
        }
        val customFont = Skin.customFont
        root.addView(fontRow("custom", "Свой .ttf", customFont))
        if (customFont != null) {
            val del = rowButton("Удалить свой шрифт", customFont.name) {
                Skin.save(this) { e ->
                    e.putString("font", "unbounded")
                    e.remove("font_file")
                }
                customFont.delete()
                recreate()
            }
            root.addView(del)
        }

        // ── сброс ────────────────────────────────────────────────────
        val reset = TextView(this)
        reset.text = "Сбросить оформление"
        reset.gravity = Gravity.CENTER
        reset.setTextColor(0xFFFF6B6B.toInt())
        reset.setTextSize(13f)
        reset.setTypeface(reset.typeface, Typeface.BOLD)
        reset.setBackgroundResource(R.drawable.bg_ripple_14)
        reset.setPadding(0, dp(14), 0, dp(14))
        reset.isClickable = true
        reset.setOnClickListener {
            Skin.save(this) { e ->
                e.putInt("accent", Color.WHITE)
                e.putFloat("surface_alpha", 1f)
                e.putInt("blur", 0)
                e.putFloat("dim", 0.35f)
                e.putString("font", "unbounded")
                e.putString("bg_type", "none")
                e.remove("font_file")
            }
            recreate()
        }
        val rlp = LinearLayout.LayoutParams(-1, -2)
        rlp.topMargin = dp(26)
        root.addView(reset, rlp)

        setContentView(scroll)
    }

    // ── элементы ─────────────────────────────────────────────────────

    private fun section(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.setTextSize(10.5f)
        t.setTextColor(0xFF6E6E6E.toInt())
        t.setTypeface(t.typeface, Typeface.BOLD)
        t.letterSpacing = 0.09f
        t.setPadding(0, dp(24), 0, dp(6))
        return t
    }

    private fun hint(text: String): TextView {
        val t = TextView(this)
        t.text = text
        t.setTextSize(11.5f)
        t.setTextColor(0xFF7A7A7A.toInt())
        t.setPadding(0, 0, 0, dp(2))
        return t
    }

    private fun swatch(color: Int): View {
        val v = View(this)
        val outer = FrameLayout(this)
        val size = dp(40)
        outer.layoutParams = LinearLayout.LayoutParams(size, size).apply {
            marginEnd = dp(10)
        }
        val circle = GradientCircle(this, color)
        outer.addView(circle, FrameLayout.LayoutParams(dp(28), dp(28)).apply {
            gravity = Gravity.CENTER
        })
        if (color == Skin.accent) {
            val ring = GradientCircle(this, Color.WHITE, stroke = true)
            outer.addView(ring, FrameLayout.LayoutParams(dp(38), dp(38)).apply {
                gravity = Gravity.CENTER
            })
        }
        outer.setOnClickListener {
            Skin.save(this) { e -> e.putInt("accent", color) }
            Skin.apply(this)
            rebuild()
        }
        return outer
    }

    /** Простой круглый цветной вью без drawable-файлов. */
    private class GradientCircle(
        ctx: android.content.Context,
        color: Int,
        private val stroke: Boolean = false,
    ) : View(ctx) {
        private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            if (stroke) {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = ctx.resources.displayMetrics.density * 1.5f
                this.color = Color.WHITE
            } else {
                this.color = color
            }
        }

        override fun onDraw(canvas: android.graphics.Canvas) {
            val r = minOf(width, height) / 2f - paint.strokeWidth
            canvas.drawCircle(width / 2f, height / 2f, r, paint)
        }
    }

    private fun rowButton(titleText: String, sub: String, click: () -> Unit): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(10), dp(12), dp(10), dp(12))
        row.setBackgroundResource(R.drawable.bg_row_ripple)

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        val t = TextView(this)
        t.text = titleText
        t.setTextColor(Color.WHITE)
        t.setTextSize(14.5f)
        t.setTypeface(t.typeface, Typeface.BOLD)
        col.addView(t)
        val s = TextView(this)
        s.text = sub
        s.setTextColor(0xFF8A8A8A.toInt())
        s.setTextSize(11.5f)
        s.setPadding(0, dp(2), 0, 0)
        col.addView(s)
        row.addView(col, LinearLayout.LayoutParams(0, -2, 1f))

        val chev = TextView(this)
        chev.text = "›"
        chev.setTextColor(0xFF8A8A8A.toInt())
        chev.setTextSize(20f)
        row.addView(chev)

        row.setOnClickListener { click() }
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.topMargin = dp(6)
        row.layoutParams = lp
        return row
    }

    private fun bgButton(text: String, click: () -> Unit): TextView {
        val t = TextView(this)
        t.text = text
        t.gravity = Gravity.CENTER
        t.setTextSize(11f)
        t.maxLines = 1
        t.ellipsize = android.text.TextUtils.TruncateAt.END
        t.setTypeface(t.typeface, Typeface.BOLD)
        t.setTextColor(Color.WHITE)
        t.setBackgroundResource(R.drawable.bg_chip)
        t.backgroundTintList = ColorStateList.valueOf(Skin.accent)
        t.setPadding(dp(6), dp(12), dp(6), dp(12))
        t.setOnClickListener { click() }
        val lp = LinearLayout.LayoutParams(0, -2, 1f)
        lp.marginEnd = dp(6)
        t.layoutParams = lp
        return t
    }

    private fun fontRow(id: String, name: String, file: File?): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER_VERTICAL
        row.setPadding(dp(10), dp(12), dp(10), dp(12))
        row.setBackgroundResource(R.drawable.bg_row_ripple)

        val sample = TextView(this)
        sample.text = name
        sample.setTextSize(15f)
        // превью каждого шрифта — его собственным начертанием
        val tf = when (id) {
            "unbounded" -> ResourcesCompat.getFont(this, R.font.unbounded_family)
            "manrope" -> ResourcesCompat.getFont(this, R.font.manrope)
            "inter" -> ResourcesCompat.getFont(this, R.font.inter)
            "jetbrains" -> ResourcesCompat.getFont(this, R.font.jetbrains_mono)
            "ptserif" -> ResourcesCompat.getFont(this, R.font.pt_serif)
            "custom" -> file?.let { runCatching { Typeface.createFromFile(it) }.getOrNull() }
            else -> null
        }
        if (tf != null) sample.typeface = tf
        sample.setTextColor(if (id == Skin.fontId) Skin.accent else Color.WHITE)
        row.addView(sample, LinearLayout.LayoutParams(0, -2, 1f))

        if (id == Skin.fontId) {
            val check = TextView(this)
            check.text = "✓"
            check.setTextColor(Skin.accent)
            check.setTextSize(15f)
            row.addView(check)
        }

        row.setOnClickListener {
            if (id == "custom") {
                pickFontFile()
            } else {
                Skin.save(this) { e -> e.putString("font", id) }
                recreate()
            }
        }
        val lp = LinearLayout.LayoutParams(-1, -2)
        lp.topMargin = dp(4)
        row.layoutParams = lp
        return row
    }

    private fun sliderRow(
        titleText: String,
        value: Float,
        min: Float,
        max: Float,
        step: Float,
        fmt: (Float) -> String,
        live: Boolean = true,
        onChange: (Float) -> Unit,
    ): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.VERTICAL
        row.setPadding(0, dp(14), 0, 0)

        val head = LinearLayout(this)
        head.orientation = LinearLayout.HORIZONTAL
        head.gravity = Gravity.CENTER_VERTICAL
        val t = TextView(this)
        t.text = titleText
        t.setTextColor(0xFFDADADA.toInt())
        t.setTextSize(13.5f)
        t.setTypeface(t.typeface, Typeface.BOLD)
        head.addView(t, LinearLayout.LayoutParams(0, -2, 1f))

        val valTx = TextView(this)
        valTx.text = fmt(value)
        valTx.setTextColor(Skin.accent)
        valTx.setTextSize(13f)
        valTx.setTypeface(valTx.typeface, Typeface.BOLD)
        head.addView(valTx)
        row.addView(head)

        val sb = SeekBar(this)
        sb.max = ((max - min) / step).roundToInt().coerceAtLeast(1)
        sb.progress = (((value - min) / step).roundToInt()).coerceIn(0, sb.max)
        sb.progressTintList = ColorStateList.valueOf(Skin.accent)
        sb.thumbTintList = ColorStateList.valueOf(Skin.accent)
        var pending = value
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                val v = (min + progress * step).coerceIn(min, max)
                valTx.text = fmt(v)
                pending = v
                if (live) onChange(v)
            }

            override fun onStartTrackingTouch(bar: SeekBar?) = Unit
            override fun onStopTrackingTouch(bar: SeekBar?) {
                if (!live) onChange(pending)
            }
        })
        row.addView(sb, LinearLayout.LayoutParams(-1, -2))
        return row
    }

    // ── выбор файлов ─────────────────────────────────────────────────

    private fun pick(request: Int, mime: String) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mime
        }
        runCatching { startActivityForResult(i, request) }
            .onFailure { toast("Нет приложения для выбора файла") }
    }

    private fun pickFontFile() {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/octet-stream"))
        }
        runCatching { startActivityForResult(i, pickFont) }
            .onFailure { toast("Нет приложения для выбора файла") }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        when (requestCode) {
            pickWallpaper -> installMedia(uri, "wallpaper")
            pickGif -> installMedia(uri, "gif")
            pickVideo -> installMedia(uri, "video")
            pickFont -> installFont(uri)
        }
    }

    private fun installMedia(uri: Uri, type: String) {
        val ext = when (type) {
            "video" -> "mp4"
            "gif" -> "gif"
            else -> "jpg"
        }
        val dst = File(Skin.directory(this), "bg_$type.$ext")
        runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                dst.outputStream().use { out -> input.copyTo(out) }
            }
        }.onFailure {
            toast("Не удалось прочитать файл")
            return
        }
        // старые фоны других типов больше не нужны
        Skin.directory(this).listFiles()?.forEach { f ->
            if (f.name.startsWith("bg_") && f != dst) runCatching { f.delete() }
        }
        Skin.save(this) { e ->
            e.putString("bg_type", type)
            e.putString("bg_file", dst.absolutePath)
        }
        Skin.apply(this)
        rebuild()
        toast("Фон установлен: ${Skin.bgTitle()}")
    }

    private fun installFont(uri: Uri) {
        val dst = File(Skin.directory(this), "custom.ttf")
        runCatching {
            contentResolver.openInputStream(uri)?.use { input ->
                dst.outputStream().use { out -> input.copyTo(out) }
            }
        }.onFailure {
            toast("Не удалось прочитать файл")
            return
        }
        val ok = runCatching { Typeface.createFromFile(dst) }.isSuccess
        if (!ok) {
            dst.delete()
            toast("Это не похоже на шрифт")
            return
        }
        Skin.save(this) { e ->
            e.putString("font", "custom")
            e.putString("font_file", dst.absolutePath)
        }
        recreate()
    }

    private fun showColorPicker() {
        val d = Dialog(this, R.style.Theme_Wonder_Sheet)
        Skin.prepare(d)
        d.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val frame = FrameLayout(this)
        frame.setBackgroundColor(0xB3000000.toInt())
        frame.setOnClickListener { d.dismiss() }

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(R.drawable.bg_dialog)
        card.setPadding(dp(20), dp(18), dp(20), dp(16))
        card.setOnClickListener { }
        frame.addView(card, FrameLayout.LayoutParams(dp(300), -2).apply {
            gravity = Gravity.CENTER
        })

        val title = TextView(this)
        title.text = "Свой цвет"
        title.setTextColor(Color.WHITE)
        title.setTextSize(17f)
        title.setTypeface(title.typeface, Typeface.BOLD)
        card.addView(title)

        var r = Color.red(Skin.accent)
        var g = Color.green(Skin.accent)
        var b = Color.blue(Skin.accent)

        val preview = View(this)
        preview.setBackgroundColor(Color.rgb(r, g, b))
        val plp = LinearLayout.LayoutParams(-1, dp(56))
        plp.topMargin = dp(12)
        card.addView(preview, plp)

        fun addChannel(name: String, value: Int, onChange: (Int) -> Unit) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(0, dp(8), 0, 0)
            val lbl = TextView(this)
            lbl.text = name
            lbl.setTextColor(0xFFBDBDBD.toInt())
            lbl.setTextSize(12f)
            lbl.width = dp(20)
            row.addView(lbl)
            val sb = SeekBar(this)
            sb.max = 255
            sb.progress = value
            sb.progressTintList = ColorStateList.valueOf(Skin.accent)
            sb.thumbTintList = ColorStateList.valueOf(Skin.accent)
            sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    onChange(progress)
                    preview.setBackgroundColor(Color.rgb(r, g, b))
                }

                override fun onStartTrackingTouch(bar: SeekBar?) = Unit
                override fun onStopTrackingTouch(bar: SeekBar?) = Unit
            })
            row.addView(sb, LinearLayout.LayoutParams(0, -2, 1f))
            card.addView(row)
        }

        addChannel("R", r) { r = it }
        addChannel("G", g) { g = it }
        addChannel("B", b) { b = it }

        val ok = TextView(this)
        ok.text = "Применить"
        ok.gravity = Gravity.CENTER
        ok.setTextColor(Skin.onAccent())
        ok.setTextSize(13f)
        ok.setTypeface(ok.typeface, Typeface.BOLD)
        ok.setBackgroundColor(Skin.accent)
        ok.setPadding(0, dp(12), 0, dp(12))
        val olp = LinearLayout.LayoutParams(-1, -2)
        olp.topMargin = dp(16)
        ok.setOnClickListener {
            Skin.save(this) { e -> e.putInt("accent", Color.rgb(r, g, b)) }
            d.dismiss()
            rebuild()
        }
        card.addView(ok, olp)

        d.setContentView(frame)
        d.show()
        runCatching {
            d.window?.setLayout(-1, -1)
            Skin.applyAccent(d.window?.decorView)
        }
    }

    private fun rebuild() {
        // Тема/шрифт читаются при создании вьюх, поэтому честный recreate,
        // а не ручная пересборка дерева (иначе можно потерять content-фрейм).
        recreate()
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}
