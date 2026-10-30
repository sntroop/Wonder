package su.gwarden.music.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Тонкая полоска прогресса шортса: сколько из «лучшего куска» уже сыграно.
 * Не интерактивная (тапать по ней нечего) - в отличие от ProgressStrip.
 */
class ClipProgress @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var played: Float = 0f
        set(v) {
            field = v.coerceIn(0f, 1f)
            invalidate()
        }

    /** Акцентный цвет из оформления. */
    var accent: Int = 0xFFFFFFFF.toInt()
        set(v) {
            field = v
            playPaint.color = v
            invalidate()
        }

    private val d = resources.displayMetrics.density
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x33FFFFFF }
    private val playPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }

    override fun onDraw(canvas: Canvas) {
        val h = 3f * d
        val cy = height / 2f
        canvas.drawRoundRect(0f, cy - h / 2f, width.toFloat(), cy + h / 2f, h / 2f, h / 2f, trackPaint)
        val w = width * played
        if (w > 0.5f) {
            canvas.drawRoundRect(0f, cy - h / 2f, w, cy + h / 2f, h / 2f, h / 2f, playPaint)
        }
    }
}
