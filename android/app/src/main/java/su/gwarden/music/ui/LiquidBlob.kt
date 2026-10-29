package su.gwarden.music.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.PathInterpolator
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.PI

/**
 * Универсальная «жидкая» подсветка: белая капля-пилюля, которая при смене
 * выбранного элемента РАСТЯГИВАЕТСЯ и перетекает на новое место, а не
 * перескакивает.
 *
 * Используется:
 *  - под верхними чипсами («Сейчас играет / Поиск / Моя волна / Библиотека»);
 *  - под строкой играющего трека в списке.
 *
 * Внутри: две пилюли (голова и отстающий хвост) + «шея» между ними рисуются
 * в офскрин-битмап с размытием, затем порог по альфе даёт эффект слипания
 * капель (metaball), как в Lottie «Liquid Tab Bar».
 */
class LiquidBlob @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    /** Прозрачность заливки 0..255. */
    /** Цвет капли. Заливка ВСЕГДА непрозрачная - так нет слоёв и обрезков. */
    var fillColor: Int = 0xFFFFFFFF.toInt()
        set(value) {
            field = value
            outPaint.colorFilter = filterFor(value)
            invalidate()
        }

    private val from = RectF()
    private val to = RectF()
    private var fromR = 0f
    private var toR = 0f
    private var progress = 1f
    private var initialized = false
    private var animator: ValueAnimator? = null

    val isAnimating: Boolean get() = animator?.isRunning == true

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        maskFilter = BlurMaskFilter(dp(7f), BlurMaskFilter.Blur.NORMAL)
    }

    private val neckPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(dp(7f), BlurMaskFilter.Blur.NORMAL)
    }

    /**
     * Порог по альфе (размытые капли -> чёткий «жидкий» край) + нужная прозрачность.
     *
     * Капли рисуются сразу с alpha = fillAlpha, а матрица делает порог ровно
     * на половине этой альфы: out = 2*a - fillAlpha. Так не нужен saveLayer
     * на весь экран (иначе на каждом кадре создаётся полноэкранный слой).
     */
    /**
     * Жёсткий порог по альфе + нужный цвет.
     *
     * Порог обязательно крутой (9a - 1030), иначе край расплывается.
     * Цвет заливки подставляем смещением в RGB-строках, а альфу оставляем
     * от порога - так капля всегда непрозрачная, без слоёв (слой раньше
     * обрезал каплю при скролле - «недорисованная плашка»).
     */
    private fun filterFor(color: Int): ColorMatrixColorFilter {
        val r = android.graphics.Color.red(color).toFloat()
        val g = android.graphics.Color.green(color).toFloat()
        val b = android.graphics.Color.blue(color).toFloat()
        return ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    0f, 0f, 0f, 0f, r,
                    0f, 0f, 0f, 0f, g,
                    0f, 0f, 0f, 0f, b,
                    0f, 0f, 0f, 9f, -1030f,
                )
            )
        )
    }

    private val outPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = filterFor(0xFFFFFFFF.toInt())
    }

    private var bmp: Bitmap? = null
    private var bmpCanvas: Canvas? = null

    // кэш: в спокойном состоянии (t == 1) форма не меняется, при скролле
    // меняется только позиция - пересчитывать размытие каждый кадр не нужно
    private val cachedRect = RectF()
    private var cachedRadius = -1f
    private var cacheValid = false

    fun setTarget(rect: RectF, radius: Float, animated: Boolean) {
        if (rect.width() <= 0f || rect.height() <= 0f) {
            hide()
            return
        }
        // если капля была спрятана - не летим из старого места, а просто встаём
        val canAnimate = animated && visibility == VISIBLE
        if (initialized && abs(rect.left - to.left) < 0.5f && abs(rect.top - to.top) < 0.5f &&
            abs(rect.right - to.right) < 0.5f && abs(rect.bottom - to.bottom) < 0.5f
        ) {
            if (visibility != VISIBLE) visibility = VISIBLE
            return
        }
        if (!initialized) {
            from.set(rect); to.set(rect)
            fromR = radius; toR = radius
            progress = 1f
            initialized = true
            visibility = VISIBLE
            invalidate()
            return
        }
        if (!canAnimate) {
            animator?.cancel()
            from.set(rect); to.set(rect)
            fromR = radius; toR = radius
            progress = 1f
            visibility = VISIBLE
            invalidate()
            return
        }
        // стартуем из того места, где капля сейчас нарисована
        from.set(
            lerp(from.left, to.left, progress),
            lerp(from.top, to.top, progress),
            lerp(from.right, to.right, progress),
            lerp(from.bottom, to.bottom, progress),
        )
        fromR = lerp(fromR, toR, progress)
        to.set(rect)
        toR = radius
        progress = 0f
        visibility = VISIBLE

        animator?.cancel()
        cacheValid = false
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 360L
            interpolator = PathInterpolator(0.22f, 0.9f, 0.24f, 1f)
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun hide() {
        animator?.cancel()
        cacheValid = false
        visibility = GONE
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    private fun smoothstep(e0: Float, e1: Float, x: Float): Float {
        val t = ((x - e0) / (e1 - e0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bmp?.recycle()
        bmp = null
        bmpCanvas = null
    }

    override fun onDraw(canvas: Canvas) {
        // это декор: любая ошибка отрисовки не должна ронять приложение
        runCatching { drawBlob(canvas) }
    }

    private fun drawBlob(canvas: Canvas) {
        if (!initialized) return
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        val t = progress.coerceIn(0f, 1f)
        val cur = RectF(
            lerp(from.left, to.left, t), lerp(from.top, to.top, t),
            lerp(from.right, to.right, t), lerp(from.bottom, to.bottom, t),
        )
        val curR = lerp(fromR, toR, t)

        val tt = (t * 0.55f).coerceIn(0f, 1f)
        val tail = RectF(
            lerp(from.left, to.left, tt), lerp(from.top, to.top, tt),
            lerp(from.right, to.right, tt), lerp(from.bottom, to.bottom, tt),
        )
        // хвост «втягивается» в голову на последней трети пути
        val drain = 1f - smoothstep(0.62f, 1f, t)
        val tailCx = tail.centerX()
        val tailCy = tail.centerY()
        val tailW = tail.width() * drain
        val tailH = tail.height() * drain
        tail.set(
            tailCx - tailW / 2f, tailCy - tailH / 2f,
            tailCx + tailW / 2f, tailCy + tailH / 2f,
        )

        val sameShape = cacheValid && bmp != null && t >= 1f && cachedRadius == curR &&
            abs(cachedRect.width() - cur.width()) < 0.5f &&
            abs(cachedRect.height() - cur.height()) < 0.5f
        if (sameShape) {
            val dx = cur.left - cachedRect.left
            val dy = cur.top - cachedRect.top
            canvas.save()
            canvas.translate(dx, dy)
            drawBmp(canvas, cachedRect)
            canvas.restore()
            return
        }

        // маленькие капли (чипсы) считаем в полном разрешении - край чёткий;
        // большие (подсветка списка на весь экран) - в половинном, иначе тяжело
        val ds = if (w.toLong() * h > 400_000L) 0.5f else 1f
        val bw = (w * ds).toInt().coerceAtLeast(1)
        val bh = (h * ds).toInt().coerceAtLeast(1)
        if (bmp == null || bmp!!.width != bw || bmp!!.height != bh) {
            bmp?.recycle()
            bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
            bmpCanvas = Canvas(bmp!!)
        }
        val c = bmpCanvas ?: return
        c.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
        c.save()
        c.scale(ds, ds)

        // лёгкое вытягивание на ходу
        val stretch = sin(t * PI).toFloat()
        c.save()
        c.scale(1f + 0.02f * stretch, 1f + 0.02f * stretch, cur.centerX(), cur.centerY())

        paint.alpha = 255
        neckPaint.alpha = 255
        c.drawRoundRect(cur, curR, curR, paint)
        if (tailW > 1f && tailH > 1f) {
            c.drawRoundRect(tail, curR, curR, paint)
            val short1 = min(cur.width(), cur.height())
            val short2 = min(tail.width(), tail.height())
            neckPaint.strokeWidth = min(short1, short2) * 0.92f
            c.drawLine(tail.centerX(), tail.centerY(), cur.centerX(), cur.centerY(), neckPaint)
        }
        c.restore()
        c.restore()

        drawBmp(canvas, cur)
        cachedRect.set(cur)
        cachedRadius = curR
        cacheValid = true
    }

    /** Рисуем готовую форму (цвет и порог уже в outPaint). */
    private fun drawBmp(canvas: Canvas, unused: RectF) {
        val b = bmp ?: return
        canvas.drawBitmap(b, null, RectF(0f, 0f, width.toFloat(), height.toFloat()), outPaint)
    }

    private companion object {
        const val DOWNSCALE = 0.5f
    }
}
