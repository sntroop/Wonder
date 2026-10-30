package su.gwarden.music.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Полоска прогресса как в Telegram/VK:
 *   - белая (полупрозрачная) часть = сколько трека уже загружено,
 *   - синяя часть = где сейчас играет,
 *   - белый кружок = ползунок, его можно тащить пальцем (seek).
 */
class ProgressStrip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    var played: Float = 0f
        set(v) {
            field = v.coerceIn(0f, 1f)
            invalidate()
        }

    var buffered: Float = 0f
        set(v) {
            field = v.coerceIn(0f, 1f)
            invalidate()
        }

    /** Колбэк по отпусканию пальца: 0..1 */
    var onSeek: ((Float) -> Unit)? = null

    /** Акцентный цвет из оформления (белый по умолчанию). */
    var accent: Int = 0xFFFFFFFF.toInt()
        set(v) {
            field = v
            playPaint.color = v
            thumbPaint.color = v
            invalidate()
        }

    private val d = resources.displayMetrics.density
    private val barH = 4f * d
    private val thumbR = 6.5f * d

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x2EFFFFFF.toInt() }
    private val bufPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x73FFFFFF.toInt() }
    private val playPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() }
    private val rect = RectF()

    private var dragging = false

    /** Палец сейчас тащит ползунок (чтобы тик не дёргал прогресс). */
    val isDragging: Boolean get() = dragging

    override fun onDraw(canvas: Canvas) {
        val cy = height / 2f
        val left = thumbR
        val right = width - thumbR
        val w = right - left

        rect.set(left, cy - barH / 2f, right, cy + barH / 2f)
        canvas.drawRoundRect(rect, barH / 2f, barH / 2f, trackPaint)

        if (buffered > 0.001f) {
            rect.set(left, cy - barH / 2f, left + w * buffered, cy + barH / 2f)
            canvas.drawRoundRect(rect, barH / 2f, barH / 2f, bufPaint)
        }
        if (played > 0.001f) {
            rect.set(left, cy - barH / 2f, left + w * played, cy + barH / 2f)
            canvas.drawRoundRect(rect, barH / 2f, barH / 2f, playPaint)
        }
        canvas.drawCircle(left + w * played, cy, thumbR, thumbPaint)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun posOf(x: Float): Float {
        val left = thumbR
        val w = (width - 2 * thumbR).coerceAtLeast(1f)
        return ((x - left) / w).coerceIn(0f, 1f)
    }

    init {
        // без этого некоторые родители (скроллы/свайпы) перехватывают палец
        isClickable = true
        isFocusable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                try {
                    su.gwarden.music.api.ApiClient.log("seek", "down x=" + event.x.toInt() + "/" + width)
                } catch (e: Throwable) {
                    // лог не должен мешать
                }
                dragging = true
                played = posOf(event.x)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragging) played = posOf(event.x)
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) {
                    val p = posOf(event.x)
                    played = p
                    onSeek?.invoke(p)
                }
                dragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
                performClick()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                dragging = false
                parent?.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
