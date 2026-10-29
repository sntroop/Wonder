package su.gwarden.music.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator

/**
 * Индикатор загрузки в стиле iOS (UIActivityIndicator):
 * 12 сужающихся «лучей», плавно вращаются по кругу.
 *
 * Почему свой: стандартный ProgressBar выглядит как системная «крутилка»
 * Material и выбивается из монохромного стиля.
 */
class IosSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 900L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { invalidate() }
    }

    /** Цвет лучей (по умолчанию белый). */
    var color: Int = 0xFFFFFFFF.toInt()
        set(value) {
            field = value
            invalidate()
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (visibility == VISIBLE && !animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            if (!animator.isStarted) animator.start()
        } else {
            animator.cancel()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val t = (animator.animatedValue as? Float) ?: 0f
        val cx = w / 2f
        val cy = h / 2f
        val radius = minOf(w, h) / 2f
        val barW = radius * 0.24f
        val barH = radius * 0.55f
        val inner = radius * 0.15f

        paint.color = color
        canvas.save()
        // всё колесо медленно вращается
        canvas.rotate(t * 360f, cx, cy)
        for (i in 0 until 12) {
            // яркость: голова яркая, хвост прозрачный
            val a = (i + 1) / 12f
            paint.alpha = (255 * (0.10f + 0.90f * a * a)).toInt().coerceIn(0, 255)
            canvas.save()
            canvas.rotate(i * 30f, cx, cy)
            val top = cy - radius + inner
            canvas.drawRoundRect(
                cx - barW / 2f, top, cx + barW / 2f, top + barH,
                barW / 2f, barW / 2f, paint
            )
            canvas.restore()
        }
        canvas.restore()
    }
}
