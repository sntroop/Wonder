package su.gwarden.music.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.view.animation.PathInterpolator
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Жидкий индикатор активной вкладки для нижнего бара (как в Lottie
 * «Liquid Tab Bar»): белая капля стоит под активной иконкой, а при
 * переключении РАСТЯГИВАЕТСЯ в «колбаску», перетекает к новой вкладке
 * и снова собирается в круг.
 *
 * Как сделано: капля рисуется в офскрин-битмап с BlurMaskFilter, затем
 * переносится на канвас через ColorMatrix-порог. Размытие + порог дают
 * эффект «слипания» капель (metaball), как в анимации.
 */
class LiquidNav @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private fun dp(v: Float) = v * density

    private var icons: List<View> = emptyList()
    private var index = 0
    private var progress = 1f
    private var fromX = 0f
    private var fromY = 0f
    private var toX = 0f
    private var toY = 0f
    private var radius = dp(17f)
    private var initialized = false
    private var animator: ValueAnimator? = null

    /** Цвет капли. */
    var blobColor: Int = 0xFFFFFFFF.toInt()
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = blobColor
        maskFilter = BlurMaskFilter(dp(7f), BlurMaskFilter.Blur.NORMAL)
    }

    private val neckPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = blobColor
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        maskFilter = BlurMaskFilter(dp(7f), BlurMaskFilter.Blur.NORMAL)
    }

    /** Порог по альфе: превращает размытые капли в чёткий «жидкий» контур. */
    private val outPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    0f, 0f, 0f, 0f, 255f,
                    0f, 0f, 0f, 0f, 255f,
                    0f, 0f, 0f, 0f, 255f,
                    0f, 0f, 0f, 9f, -1030f,
                )
            )
        )
    }

    // нав маленький - считаем в полном разрешении, край получается чётким
    private val DOWNSCALE = 1.0f
    private var bmp: Bitmap? = null
    private var bmpCanvas: Canvas? = null

    fun bindIcons(list: List<View>) {
        icons = list
        radius = radiusFor()
        post { setActive(index, false) }
    }

    fun setActive(i: Int, animated: Boolean = true, attempt: Int = 0) {
        if (icons.isEmpty() || i < 0) {
            index = i
            return
        }
        val target = centerOf(i)
        if (target == null) {
            index = i
            if (attempt < 40) postDelayed({ setActive(i, animated, attempt + 1) }, 40L)
            return
        }
        if (!initialized) {
            fromX = target.x; fromY = target.y
            toX = target.x; toY = target.y
            progress = 1f
            initialized = true
            invalidate()
            return
        }
        val old = centerOf(index)
        index = i
        radius = radiusFor()

        // уже стоим там - никаких «дёрганий»
        if (old != null && kotlin.math.abs(old.x - target.x) < 0.5f &&
            kotlin.math.abs(old.y - target.y) < 0.5f
        ) {
            fromX = target.x; fromY = target.y
            toX = target.x; toY = target.y
            progress = 1f
            invalidate()
            return
        }

        if (!animated || old == null) {
            fromX = target.x; fromY = target.y
            toX = target.x; toY = target.y
            progress = 1f
            invalidate()
            return
        }

        // стартуем из текущей точки (если прошлая анимация не доехала)
        fromX = fromX + (toX - fromX) * progress
        fromY = fromY + (toY - fromY) * progress
        toX = target.x
        toY = target.y
        progress = 0f

        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 440L
            interpolator = PathInterpolator(0.22f, 0.9f, 0.24f, 1f)
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun radiusFor(): Float {
        val ic = icons.firstOrNull { it.height > 0 } ?: return dp(18f)
        return ic.height / 2f + dp(7f)
    }

    /**
     * Центр иконки В КООРДИНАТАХ ЭТОГО View.
     *
     * ГРАБЛЯ: иконки лежат внутри своих ячеек-LinearLayout, поэтому v.left/v.top
     * у всех четырёх ОДИНАКОВЫЕ (координаты внутри своей ячейки). Именно из-за
     * этого капля стояла на «домике» и только дёргалась. Считаем через
     * getLocationInWindow - это работает при любой вложенности.
     */
    private fun centerOf(i: Int): PointF? {
        val v = icons.getOrNull(i) ?: return null
        if (v.width == 0 || v.height == 0 || width == 0) return null
        val lv = IntArray(2)
        val ls = IntArray(2)
        v.getLocationInWindow(lv)
        getLocationInWindow(ls)
        if (lv[0] == 0 && lv[1] == 0) return null
        return PointF(
            (lv[0] - ls[0]) + v.width / 2f,
            (lv[1] - ls[1]) + v.height / 2f,
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bmp?.recycle()
        bmp = null
        bmpCanvas = null
    }

    private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        if (!initialized) return
        val t = progress.coerceIn(0f, 1f)
        val x = fromX + (toX - fromX) * t
        val y = fromY + (toY - fromY) * t
        // «хвост» отстаёт и тает - получается перетекание, а не перескок
        val tailT = (t * 0.55f).coerceIn(0f, 1f)
        val tx = fromX + (toX - fromX) * tailT
        val ty = fromY + (toY - fromY) * tailT
        // хвост держит форму, а на последней трети «втягивается» в голову
        val drain = smoothstep(0.60f, 1f, t)
        val tailR = radius * (1f - drain)

        // считаем в половинном разрешении: размытие в 4 раза дешевле,
        // на экране разницы не видно (порог всё равно даёт чёткий край)
        val bw = (w * DOWNSCALE).toInt().coerceAtLeast(1)
        val bh = (h * DOWNSCALE).toInt().coerceAtLeast(1)
        if (bmp == null || bmp!!.width != bw || bmp!!.height != bh) {
            bmp?.recycle()
            bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
            bmpCanvas = Canvas(bmp!!)
        }
        val c = bmpCanvas ?: return
        c.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)

        c.save()
        c.scale(DOWNSCALE, DOWNSCALE)
        // лёгкое «сплющивание» на ходу - капля вытягивается
        val stretch = sin(t * PI).toFloat()
        c.scale(1f + 0.10f * stretch, 1f - 0.08f * stretch, x, y)

        c.drawCircle(x, y, radius, paint)
        if (tailR > 1f) {
            c.drawCircle(tx, ty, tailR, paint)
            // шея почти равна каплям - без ступеньки на стыке
            neckPaint.strokeWidth = 2f * min(radius, tailR) * 0.98f
            c.drawLine(tx, ty, x, y, neckPaint)
        }
        c.restore()

        canvas.drawBitmap(bmp!!, null, android.graphics.RectF(0f, 0f, w.toFloat(), h.toFloat()), outPaint)
    }
}
