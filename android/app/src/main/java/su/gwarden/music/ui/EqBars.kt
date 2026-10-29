package su.gwarden.music.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.abs
import kotlin.math.sin

/**
 * Минимальный эквалайзер: 4 скруглённых столбика, «дышат» в такт.
 * Показывается поверх обложки играющего трека (в списке и в плеере).
 */
class EqBars @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var color: Int = 0xFFFFFFFF.toInt()
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1100L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { invalidate() }
    }

    private val phases = floatArrayOf(0.0f, 0.45f, 0.18f, 0.62f)

    val isRunning: Boolean get() = animator.isRunning

    fun start() {
        if (!animator.isStarted) animator.start()
        visibility = VISIBLE
    }

    fun stop() {
        animator.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (visibility == VISIBLE && !animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val t = (animator.animatedValue as? Float) ?: 0f
        val n = 4
        val gap = w * 0.14f
        val bw = (w - gap * (n - 1)) / n
        val r = bw / 2f

        paint.color = color
        for (i in 0 until n) {
            val ph = (t + phases[i]) * 2f * Math.PI.toFloat()
            // 0..1, но не даём столбику «умереть» совсем
            val k = 0.30f + 0.70f * abs(sin(ph.toDouble())).toFloat()
            val bh = h * k
            val left = i * (bw + gap)
            val rect = RectF(left, h - bh, left + bw, h)
            canvas.drawRoundRect(rect, r, r, paint)
        }
    }
}
