package su.gwarden.music.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Фон «Моей волны»: мягкий туман одним полотном.
 *
 * Данные о громкости и ударах приходят с сервера (анализ самого mp3 через
 * ffmpeg) — поэтому фон реально попадает в трек, а не живёт сам по себе.
 * Никаких микрофонов и Visualizer'ов: приложение просто листает карту
 * энергии по текущей позиции плеера.
 */
class EqBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    /** Удар (для пульса обложки). */
    var onBeat: (() -> Unit)? = null

    /** Откуда брать позицию трека, мс. */
    var positionProvider: (() -> Int)? = null

    /**
     * Пересчёт центра/радиуса обложки. Экран сам знает, вокруг чего рисовать,
     * а фон раз в ~240 мс переспрашивает: после смены раскладки (мини-плеер,
     * системные панели, поворот) лучи больше не остаются на старом месте.
     */
    var orbSync: (() -> Unit)? = null

    private val d = resources.displayMetrics.density
    private fun dp(v: Float) = v * d
    private var t = 0f
    private var level = 0f
    private var smooth = 0f
    private var levelAvg = 0.3f
    private var beat = 0f
    private var lastBeatPos = -100000
    private var active = false
    private var looping = false

    private var energy = FloatArray(0)
    private var rate = 25f
    private var beats = FloatArray(0)
    private var beatIdx = 0
    private var lastSec = 0f
    private var nowMs = 0L
    private var frameNo = 0
    private var lastOrbSync = 0L
    private var spectrum = ""
    private val bars = FloatArray(8)
    // цвета берём из обложки трека (первый - основной)
    private var palette = intArrayOf(0xFFFFFF, 0xCFCFCF, 0xB0B0B0, 0xD9D9D9)

    private var fog: Bitmap? = null
    private var fogW = 0
    private var fogH = 0

    private var orbCx = 0f
    private var orbCy = 0f
    private var orbR = 0f
    private var glowShader: RadialGradient? = null

    private val fogPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dst = RectF()

    private val loop = object : Runnable {
        override fun run() {
            if (!active) {
                looping = false
                return
            }
            t += 0.05f
            nowMs = System.currentTimeMillis()
            if (nowMs - lastOrbSync > 240L) {
                lastOrbSync = nowMs
                runCatching { orbSync?.invoke() }
            }

            val pos = positionProvider?.invoke() ?: -1
            if (pos >= 0) {
                val sec = pos / 1000f
                if (sec < lastSec - 0.6f) {
                    beatIdx = 0     // трек начался заново / перескочили назад
                }
                lastSec = sec

                // уровень громкости - им «дышит» туман
                if (energy.isNotEmpty()) {
                    val idx = (sec * rate).toInt()
                    val e = if (idx in energy.indices) energy[idx] else 0f
                    level = maxOf(level * 0.72f, e)
                    levelAvg = levelAvg * 0.94f + e * 0.06f
                }

                if (beats.isNotEmpty()) {
                    // точные удары, посчитанные сервером по самому аудио
                    var hit = false
                    while (beatIdx < beats.size && beats[beatIdx] <= sec) {
                        if (sec - beats[beatIdx] < 0.14f) {
                            hit = true
                        }
                        beatIdx++
                    }
                    if (hit) {
                        beat = 1f
                        onBeat?.invoke()
                    }
                }

                // полосы спектра: каждая «нота» бьёт в своё место
                if (spectrum.isNotEmpty()) {
                    val off = (sec * rate).toInt() * 8
                    if (off >= 0 && off + 8 <= spectrum.length) {
                        for (k in 0 until 8) {
                            val v = Character.digit(spectrum[off + k], 16)
                                .coerceAtLeast(0) / 15f
                            bars[k] = maxOf(bars[k] * 0.62f, v)
                        }
                    }
                } else if (energy.isNotEmpty()) {
                    // запасной путь: удар = резкий рост энергии
                    val idx = (sec * rate).toInt()
                    val e = if (idx in energy.indices) energy[idx] else 0f
                    val prev = if (idx - 2 in energy.indices) energy[idx - 2] else 0f
                    if (e > 0.22f && e > levelAvg * 1.18f && e - prev > 0.08f
                        && pos - lastBeatPos > 170) {
                        lastBeatPos = pos
                        beat = 1f
                        onBeat?.invoke()
                    }
                }
            } else {
                level *= 0.92f
            }
            smooth = smooth * 0.74f + level * 0.26f
            beat *= 0.94f

            invalidate()
            postDelayed(this, 33)
        }
    }

    // ------------------------------------------------------------ данные

    fun setBeatData(energyData: FloatArray, rateHz: Float) {
        setBeatData(energyData, rateHz, FloatArray(0))
    }

    fun setBeatData(energyData: FloatArray, rateHz: Float, beatTimes: FloatArray) {
        setBeatData(energyData, rateHz, beatTimes, "")
    }

    fun setBeatData(energyData: FloatArray, rateHz: Float, beatTimes: FloatArray, bandHex: String) {
        energy = energyData
        rate = if (rateHz > 0f) rateHz else 25f
        beats = beatTimes
        spectrum = bandHex
        beatIdx = 0
    }

    /**
     * Цвета из обложки: туман окрашивается в них (размыто), а не остаётся
     * серым. Вызывать после того, как обложка скачалась.
     */
    fun setArtwork(bmp: Bitmap?) {
        if (bmp == null) return
        val colors = extractColors(bmp)
        if (colors.isEmpty()) return
        palette = colors
        if (fogW > 0 && fogH > 0) {
            buildFog(fogW, fogH)
        }
        rebuildGlow()
        invalidate()
    }

    private fun extractColors(src: Bitmap): IntArray {
        val small = runCatching { Bitmap.createScaledBitmap(src, 36, 36, true) }
            .getOrNull() ?: return IntArray(0)
        val buckets = HashMap<Int, IntArray>()
        for (y in 0 until small.height) {
            for (x in 0 until small.width) {
                val c = small.getPixel(x, y)
                if ((c ushr 24) and 0xFF < 200) continue
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                val mx = maxOf(r, g, b)
                val mn = minOf(r, g, b)
                if (mx < 26 || mn > 232) continue
                val key = ((r shr 5) shl 6) or ((g shr 5) shl 3) or (b shr 5)
                val arr = buckets.getOrPut(key) { IntArray(4) }
                arr[0]++
                arr[1] += r
                arr[2] += g
                arr[3] += b
            }
        }
        if (buckets.isEmpty()) return IntArray(0)
        return buckets.values
            .sortedByDescending { it[0] }
            .take(3)
            .map { a ->
                val n = a[0].coerceAtLeast(1)
                vivid((a[1] / n shl 16) or (a[2] / n shl 8) or (a[3] / n))
            }
            .toIntArray()
    }

    /** Чуть поднять яркость/насыщенность, иначе туман будет почти чёрным. */
    private fun vivid(c: Int): Int {
        var r = (c shr 16) and 0xFF
        var g = (c shr 8) and 0xFF
        var b = c and 0xFF
        val mx = maxOf(r, g, b)
        if (mx < 150) {
            val k = 150f / mx.coerceAtLeast(1)
            r = (r * k).toInt().coerceAtMost(255)
            g = (g * k).toInt().coerceAtMost(255)
            b = (b * k).toInt().coerceAtMost(255)
        }
        return (r shl 16) or (g shl 8) or b
    }

    private fun rebuildGlow() {
        if (orbR <= 0f) return
        val c = palette.firstOrNull() ?: 0xFFFFFF
        // шейдер строим вокруг (0,0): при движении обложки не пересоздаём его,
        // а просто сдвигаем canvas (иначе на каждом кадре скролла - новый градиент)
        glowShader = RadialGradient(
            0f, 0f, orbR * 2.9f,
            intArrayOf((0x7A shl 24) or c, (0x2E shl 24) or c, 0x00000000),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP,
        )
    }

    /** Центр и радиус обложки в координатах этого View. */
    fun setOrb(cx: Float, cy: Float, r: Float) {
        if (r <= 0f) return
        val resized = abs(r - orbR) > 0.25f
        orbCx = cx
        orbCy = cy
        orbR = r
        if (resized || glowShader == null) rebuildGlow()
        invalidate()
    }

    fun setActive(on: Boolean) {
        active = on
        if (on) {
            if (!looping) {
                looping = true
                post(loop)
            }
        }
    }

    fun release() {
        active = false
        energy = FloatArray(0)
        level = 0f
        smooth = 0f
        beat = 0f
    }

    override fun onDetachedFromWindow() {
        active = false
        super.onDetachedFromWindow()
    }

    // ------------------------------------------------------------ туман

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != fogW || h != fogH) {
            buildFog(w, h)
            fogW = w
            fogH = h
        }
        post { runCatching { orbSync?.invoke() } }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    /** Один раз рисуем всё облако в bitmap половинного размера - он же и размывает. */
    private fun buildFog(w: Int, h: Int) {
        if (w <= 0 || h <= 0) return
        val bw = (w / 3).coerceAtLeast(2)
        val bh = (h / 3).coerceAtLeast(2)
        val bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val rnd = java.util.Random(11)
        val side = min(bw, bh).toFloat()

        fun blob(x: Float, y: Float, r: Float, rgb: Int, a: Int) {
            p.shader = RadialGradient(
                x, y, r,
                intArrayOf(rgb or 0xFF000000.toInt(), rgb or 0x59000000, rgb or 0x00000000),
                floatArrayOf(0f, 0.45f, 1f),
                Shader.TileMode.CLAMP,
            )
            p.alpha = a
            c.drawCircle(x, y, r, p)
        }

        // большие облака (сильно перекрываются - поэтому не видно "точек")
        for (i in 0 until 9) {
            blob(
                bw * (0.02f + rnd.nextFloat() * 0.96f),
                bh * (0.02f + rnd.nextFloat() * 0.96f),
                side * (0.30f + rnd.nextFloat() * 0.32f),
                palette[i % palette.size],
                44 + rnd.nextInt(30),
            )
        }
        // дымные волны
        for (band in 0 until 3) {
            val baseY = bh * (0.40f + band * 0.17f)
            val amp = bh * (0.05f + band * 0.02f)
            val freq = 0.9f + band * 0.55f
            val n = 26
            for (k in 0..n) {
                val fx = k.toFloat() / n
                val x = -bw * 0.10f + fx * bw * 1.2f
                val y = baseY + sin((fx * freq * 2.0 * PI + band * 1.9).toDouble()).toFloat() * amp
                blob(x, y, side * 0.16f, palette[(band + 1) % palette.size], 38)
            }
        }

        fog?.recycle()
        fog = bmp
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        fog?.let { bmp ->
            val scale = 1.09f + 0.13f * beat + 0.05f * smooth
            val ox = sin((t * 0.5f).toDouble()).toFloat() * w * 0.02f
            val oy = cos((t * 0.37f).toDouble()).toFloat() * h * 0.015f

            // основной слой
            val hw = w * scale / 2f
            val hh = h * scale / 2f
            fogPaint.alpha = (125 + 85 * smooth + 85 * beat).toInt().coerceIn(0, 255)
            dst.set(w / 2f - hw + ox, h / 2f - hh + oy, w / 2f + hw + ox, h / 2f + hh + oy)
            canvas.drawBitmap(bmp, null, dst, fogPaint)

            // дальний слой - медленный параллакс; рисуем через кадр (экономия)
            val s2 = scale * 1.20f
            val hw2 = w * s2 / 2f
            val hh2 = h * s2 / 2f
            fogPaint.alpha = (55 + 35 * smooth).toInt().coerceIn(0, 255)
            dst.set(
                w / 2f - hw2 - ox * 1.7f, h / 2f - hh2 - oy * 1.5f,
                w / 2f + hw2 - ox * 1.7f, h / 2f + hh2 - oy * 1.5f,
            )
            canvas.drawBitmap(bmp, null, dst, fogPaint)
        }
        frameNo++

        // эквалайзер ВОКРУГ обложки: 48 лучей, 8 полос с плавной склейкой
        if (orbR > 0f) {
            val n = 96
            val baseR = orbR + dp(2f)
            val maxLen = orbR * 0.40f
            val c0 = palette.firstOrNull() ?: 0xFFFFFF
            barPaint.color = (0x70 shl 24) or (c0 and 0x00FFFFFF)
            barPaint.strokeWidth = dp(3.2f)
            barPaint.strokeCap = Paint.Cap.ROUND
            barPaint.style = Paint.Style.STROKE
            val twoPi = 2.0 * PI
            for (i in 0 until n) {
                val f = i.toFloat() / n * 8f
                val i0 = f.toInt() % 8
                val i1 = (i0 + 1) % 8
                val mix = f - f.toInt()
                val v = bars[i0] * (1f - mix) + bars[i1] * mix
                if (v < 0.02f) continue
                val ang = (i.toFloat() / n) * twoPi - PI / 2.0
                val cs = cos(ang).toFloat()
                val sn = sin(ang).toFloat()
                val r1 = baseR + maxLen * v
                canvas.drawLine(
                    orbCx + cs * baseR, orbCy + sn * baseR,
                    orbCx + cs * r1, orbCy + sn * r1,
                    barPaint,
                )
            }
        }

        // мягкое свечение за обложкой - дышит вместе с треком
        glowShader?.let {
            glowPaint.shader = it
            glowPaint.alpha = (34 + 80 * beat + 55 * smooth).toInt().coerceIn(0, 255)
            canvas.save()
            canvas.translate(orbCx, orbCy)
            canvas.drawCircle(0f, 0f, orbR * 2.9f, glowPaint)
            canvas.restore()
        }
    }
}
