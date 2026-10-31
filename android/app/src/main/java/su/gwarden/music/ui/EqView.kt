package su.gwarden.music.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import su.gwarden.music.audio.Biquad
import su.gwarden.music.audio.Fx
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

/**
 * 10-полосный редактор эквалайзера: вертикальные ползунки + кривая АЧХ.
 *
 * Рисуется на канвасе, а не десятью SeekBar'ами: так полосы не «прыгают»
 * при разной ширине экрана, а кривая показывает реальную сумму фильтров.
 * Тянешь палец по полосам — значения меняются на лету.
 */
class EqView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val d = resources.displayMetrics.density

    /** Текущие усиления полос, дБ -12..+12. */
    var bands: FloatArray = FloatArray(Fx.BANDS.size)
        set(v) {
            field = v.copyOf()
            invalidate()
        }

    /** Усиление низкой полки (ползунок «Басы»), дБ 0..15. */
    var bass: Float = 0f
        set(v) {
            field = v.coerceIn(0f, 15f)
            invalidate()
        }

    /** Колбэк: полоса изменилась (индекс, дБ). */
    var onBandChanged: ((Int, Float) -> Unit)? = null

    var accent: Int = Color.WHITE
        set(v) {
            field = v
            barPaint.color = v
            curvePaint.color = Skin.withAlpha(v, 0x8A)
            handlePaint.color = v
            invalidate()
        }

    private var selected = -1

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x24FFFFFF }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x8AFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 1.6f * d
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF8A8A8A.toInt()
        textSize = 9f * d
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 11f * d
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val rect = RectF()
    private val curve = Path()

    private val padTop = 22f * d
    private val padBottom = 20f * d
    private val padSide = 6f * d

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = (220 * d).toInt()
        setMeasuredDimension(w, resolveSize(h, heightMeasureSpec))
    }

    private fun colW(): Float =
        (width - padSide * 2) / Fx.BANDS.size

    private fun centerY(): Float = padTop + (height - padTop - padBottom) / 2f

    private fun halfH(): Float = (height - padTop - padBottom) / 2f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cw = colW()
        val cy = centerY()
        val hh = halfH()

        // центральная линия 0 дБ
        canvas.drawLine(padSide, cy, width - padSide, cy, trackPaint)

        // дорожки
        for (i in 0 until Fx.BANDS.size) {
            val x = padSide + cw * i + cw / 2f
            rect.set(x - 1f * d, cy - hh, x + 1f * d, cy + hh)
            canvas.drawRoundRect(rect, 1f * d, 1f * d, trackPaint)
        }

        // кривая АЧХ (сумма 10 пиков + бас)
        drawCurve(canvas, cw, cy, hh)

        // заполнение и ручки
        for (i in 0 until Fx.BANDS.size) {
            val x = padSide + cw * i + cw / 2f
            val db = bands.getOrElse(i) { 0f }.coerceIn(-12f, 12f)
            val y = cy - hh * (db / 12f)
            val w = if (i == selected) 7f * d else 5f * d
            rect.set(x - w / 2f, minOf(cy, y), x + w / 2f, maxOf(cy, y))
            if (abs(y - cy) > 1f) {
                canvas.drawRoundRect(rect, w / 2f, w / 2f, barPaint)
            }
            canvas.drawCircle(x, y, if (i == selected) 6f * d else 4.5f * d, handlePaint)

            textPaint.color = if (i == selected) accent else 0xFF7A7A7A.toInt()
            canvas.drawText(Fx.BAND_LABELS[i], x, height - 5f * d, textPaint)
        }

        // подпись значения выбранной полосы
        if (selected in Fx.BANDS.indices) {
            val db = bands[selected]
            val x = padSide + cw * selected + cw / 2f
            valuePaint.color = accent
            val txt = (if (db > 0.05f) "+" else "") + String.format(java.util.Locale.US, "%.1f", db)
            val tx = x.coerceIn(26f * d, width - 26f * d)
            canvas.drawText("$txt dB", tx, 14f * d, valuePaint)
        }
    }

    /** Кривая суммарной АЧХ: 10 пиков, Q=1.4, плюс бас-полка. */
    private fun drawCurve(canvas: Canvas, cw: Float, cy: Float, hh: Float) {
        val fs = 44100f
        val filters = Array(Fx.BANDS.size) { Biquad() }
        for (i in Fx.BANDS.indices) {
            filters[i].peaking(fs, Fx.BANDS[i], 1.4f, bands.getOrElse(i) { 0f })
        }
        val bassFilter = Biquad().apply { lowShelf(fs, 90f, bass) }
        val steps = 120
        curve.reset()
        for (s in 0..steps) {
            val t = s / steps.toFloat()
            // 20 Гц .. 20 кГц логарифмически
            val f = 20f * 1000f.pow(t)
            var sum = 0f
            for (filt in filters) sum += filt.magnitudeDb(f, fs)
            sum += bassFilter.magnitudeDb(f, fs)
            val db = sum.coerceIn(-12f, 12f)
            val x = freqToX(f, cw)
            val y = cy - hh * (db / 12f)
            if (s == 0) curve.moveTo(x, y) else curve.lineTo(x, y)
        }
        canvas.drawPath(curve, curvePaint)
    }

    /**
     * Частота -> X. Полосы на экране стоят равномерно, а частоты октавные,
     * поэтому обычная логарифмическая шкала даёт пик правее ручки. Здесь
     * каждая полоса попадает ровно в свой центр, а края 20 Гц/20 кГц
     * продолжают шкалу до краёв графика.
     */
    private fun freqToX(f: Float, cw: Float): Float {
        val first = Fx.BANDS.first()
        val last = Fx.BANDS.last()
        if (f <= first) {
            val t = (log10((f / 20f).toDouble()) /
                    log10((first / 20f).toDouble())).toFloat().coerceIn(0f, 1f)
            return padSide + cw * 0.5f * t
        }
        if (f >= last) {
            val t = (log10((f / last).toDouble()) /
                    log10((20000f / last).toDouble())).toFloat().coerceIn(0f, 1f)
            return padSide + cw * 9.5f + cw * 0.5f * t
        }
        for (i in 0 until Fx.BANDS.size - 1) {
            if (f <= Fx.BANDS[i + 1]) {
                val t = (log10((f / Fx.BANDS[i]).toDouble()) /
                        log10((Fx.BANDS[i + 1] / Fx.BANDS[i]).toDouble())).toFloat()
                return padSide + cw * (i + 0.5f + t)
            }
        }
        return width - padSide
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun bandAt(x: Float): Int {
        val cw = colW().coerceAtLeast(1f)
        val i = ((x - padSide) / cw).toInt()
        return i.coerceIn(0, Fx.BANDS.size - 1)
    }

    private fun dbAt(y: Float): Float {
        val cy = centerY()
        val hh = halfH().coerceAtLeast(1f)
        return (-(y - cy) / hh * 12f).coerceIn(-12f, 12f)
    }

    private fun setBand(i: Int, db: Float) {
        if (i !in Fx.BANDS.indices) return
        bands[i] = db.coerceIn(-12f, 12f)
        onBandChanged?.invoke(i, bands[i])
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                selected = bandAt(event.x)
                setBand(selected, dbAt(event.y))
                performClick()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (selected >= 0) setBand(selected, dbAt(event.y))
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                selected = -1
                invalidate()
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /** Для теста/сброса: ровная линия. */
    fun flatten() {
        bands = FloatArray(Fx.BANDS.size)
        onBandChanged?.invoke(-1, 0f)
    }

    @Suppress("unused")
    private fun hzToX(f: Float, w: Float): Float =
        (log10((f / 20f).toDouble()) / log10(1000.0)).toFloat() * w
}
