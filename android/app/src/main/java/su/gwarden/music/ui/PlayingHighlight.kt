package su.gwarden.music.ui

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.animation.PathInterpolator
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/**
 * Подсветка играющего трека ПРЯМО ВНУТРИ RecyclerView (ItemDecoration).
 *
 * Почему так: раньше это был отдельный View поверх списка, и его приходилось
 * синхронизировать с прокруткой вручную - из-за этого плашка отставала и
 * «уползала» со строки. ItemDecoration рисуется в тех же координатах, что и
 * сами строки, и скроллится вместе с ними без всякой синхронизации.
 *
 * При смене трека капля перетекает: тянется от старой строки к новой.
 */
class PlayingHighlight(private val adapter: TrackAdapter) : RecyclerView.ItemDecoration() {

    var color: Int = 0xFF3A3A3A.toInt()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = this@PlayingHighlight.color }

    private var host: RecyclerView? = null
    private var fromRect: RectF? = null
    private var lastRect: RectF? = null
    private var progress = 1f
    private var animator: ValueAnimator? = null

    /** Сменился играющий трек - запускаем перелёт. */
    fun play(parent: RecyclerView) {
        // аниматоры можно только в потоке с Looper; на всякий случай
        // перекидываем в главный (иначе AndroidRuntimeException и краш)
        if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
            parent.post { play(parent) }
            return
        }
        host = parent
        fromRect = lastRect?.let { RectF(it) }
        progress = 0f
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 380L
            interpolator = PathInterpolator(0.22f, 0.9f, 0.24f, 1f)
            addUpdateListener {
                progress = it.animatedValue as Float
                host?.invalidate()
            }
            start()
        }
    }

    fun reset() {
        animator?.cancel()
        fromRect = null
        lastRect = null
        progress = 1f
        host?.invalidate()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.adapter !== adapter) {
            lastRect = null
            return
        }
        val pos = adapter.playingPosition()
        if (pos < 0) {
            lastRect = null
            return
        }
        val v = parent.layoutManager?.findViewByPosition(pos) ?: run {
            lastRect = null
            return
        }
        val d = parent.resources.displayMetrics.density
        val target = RectF(
            v.left + 2 * d,
            v.top + 1 * d,
            v.right - 2 * d,
            v.bottom - 1 * d,
        )

        val from = fromRect
        val r: RectF
        if (from != null && progress < 1f) {
            val t = progress.coerceIn(0f, 1f)
            r = RectF(
                from.left + (target.left - from.left) * t,
                from.top + (target.top - from.top) * t,
                from.right + (target.right - from.right) * t,
                from.bottom + (target.bottom - from.bottom) * t,
            )
            // пока летим - тянемся: капля вытягивается между строками
            val stretch = abs(target.top - from.top) * 0.5f * sin(t * PI).toFloat()
            r.top -= stretch
            r.bottom += stretch
        } else {
            r = target
        }

        paint.color = color
        val rad = 16f * d
        c.drawRoundRect(r, rad, rad, paint)
        lastRect = target
    }
}
