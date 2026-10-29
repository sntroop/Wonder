package su.gwarden.music.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Typeface
import su.gwarden.music.api.ApiClient
import kotlin.math.abs

/**
 * Свои обложки для плейлистов вместо разноцветных VK-мозаик: монохромный
 * градиент + мягкие блики + крупная первая буква названия. Рисунок
 * детерминированный (по seed), поэтому обложка не меняется между запусками.
 */
object PlaylistArt {

    private val palettes = arrayOf(
        intArrayOf(0xFF2B2B2B.toInt(), 0xFF060606.toInt()),
        intArrayOf(0xFF1E1E1E.toInt(), 0xFF0A0A0A.toInt()),
        intArrayOf(0xFF343434.toInt(), 0xFF101010.toInt()),
        intArrayOf(0xFF171717.toInt(), 0xFF000000.toInt()),
        intArrayOf(0xFF262626.toInt(), 0xFF0C0C0C.toInt()),
        intArrayOf(0xFF3A3A3A.toInt(), 0xFF141414.toInt()),
    )

    private val cache = android.util.LruCache<String, Bitmap>(16)

    /**
     * Обложка плейлиста в ImageView: своя картинка из файла (/data/...),
     * url из сети или сгенерированная заглушка.
     */
    fun into(
        view: android.widget.ImageView,
        cover: String,
        seed: String,
        title: String,
        sizePx: Int,
    ) {
        val gen = bitmap(view.context, seed, title, sizePx)
        when {
            cover.isEmpty() -> view.setImageBitmap(gen)
            cover.startsWith("/") -> {
                val bmp = runCatching {
                    android.graphics.BitmapFactory.decodeFile(cover)
                }.getOrNull()
                view.setImageBitmap(bmp ?: gen)
            }
            else -> ImageLoader.load(view, ApiClient.img(cover, sizePx * 2), 0, placeholderBmp = gen)
        }
    }

    /** Что печатать на обложке плейлиста. */
    fun labelFor(title: String): String {
        val t = title.trim()
        if (t.equals("Для вас", true)) return "FOR YOU"
        if (t.equals("Открытия", true)) return "OPENED"
        if (t.equals("Новинки", true)) return "NEW"
        Regex("^Подборка\\s*(\\d+)$").find(t)?.let { return it.groupValues[1] }
        return t.take(1).uppercase().ifEmpty { "·" }
    }

    fun bitmap(ctx: Context, seed: String, label: String, size: Int): Bitmap {
        val s = size.coerceAtLeast(32)
        val key = "${seed}|${label}|$s"
        cache.get(key)?.let { return it }
        val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val h = abs((seed.ifEmpty { label }).hashCode())
        val pal = palettes[h % palettes.size]

        val base = Paint(Paint.ANTI_ALIAS_FLAG)
        base.shader = LinearGradient(
            0f, 0f, s.toFloat(), s.toFloat(),
            pal[0], pal[1], Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, s.toFloat(), s.toFloat(), base)

        // мягкие пятна света
        val g1 = Paint(Paint.ANTI_ALIAS_FLAG)
        val cx = s * (0.15f + (h % 55) / 100f)
        val cy = s * (0.12f + ((h / 55) % 45) / 100f)
        g1.shader = RadialGradient(
            cx, cy, s * 0.72f,
            intArrayOf(0x24FFFFFF, 0x00FFFFFF), null, Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, s.toFloat(), s.toFloat(), g1)

        val g2 = Paint(Paint.ANTI_ALIAS_FLAG)
        g2.shader = RadialGradient(
            s * 0.88f, s * 0.92f, s * 0.6f,
            intArrayOf(0x16FFFFFF, 0x00FFFFFF), null, Shader.TileMode.CLAMP
        )
        c.drawRect(0f, 0f, s.toFloat(), s.toFloat(), g2)

        // тонкое кольцо - «что-то прикольное», но без цвета
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = s * 0.007f
            color = 0x1CFFFFFF
        }
        c.drawCircle(s * 0.79f, s * 0.21f, s * (0.20f + (h % 12) / 100f), ring)

        // надпись: «FOR YOU» для «Для вас», номер у «Подборка N», иначе первая буква
        val text = labelFor(label)
        val words = text.split(" ").filter { it.isNotEmpty() }
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xE8FFFFFF.toInt()
            textAlign = Paint.Align.CENTER
        }
        if (words.size <= 1) {
            tp.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            tp.textSize = s * 0.44f
            val maxW = s * 0.78f
            val w = tp.measureText(text.ifEmpty { "·" }).coerceAtLeast(1f)
            if (w > maxW) tp.textSize *= maxW / w
            val y = s * 0.5f - (tp.descent() + tp.ascent()) / 2f
            c.drawText(text.ifEmpty { "·" }, s * 0.5f, y, tp)
        } else {
            // брутальный постер: жирные строки на всю ширину обложки
            tp.typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            val maxW = s * 0.76f
            tp.textSize = s * 0.5f
            val widest = words.maxOf { tp.measureText(it) }.coerceAtLeast(1f)
            tp.textSize = (tp.textSize * (maxW / widest)).coerceAtMost(s * 0.34f)
            val lineH = tp.textSize * 0.98f
            val total = lineH * words.size
            words.forEachIndexed { i, w ->
                val centerY = s * 0.5f - total / 2f + lineH * (i + 0.5f)
                c.drawText(w, s * 0.5f, centerY - (tp.ascent() + tp.descent()) / 2f, tp)
            }
        }
        cache.put(key, bmp)
        return bmp
    }
}
