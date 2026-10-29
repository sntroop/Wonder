package su.gwarden.music.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * Фото, у которого низ плавно растворяется в прозрачность.
 *
 * Нужно для шапки артиста: раньше сверху клали чёрный градиент, из-за чего
 * низ фото либо резко обрывался, либо уходил в чёрный (и перекрывал цветокор
 * обложки). Здесь затухает сама картинка: маска DST_IN рисуется прямо в
 * слое ImageView, поэтому под низом виден настоящий фон приложения.
 */
class FadeImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AppCompatImageView(context, attrs) {

    /** С какой доли высоты начинается затухание (0..1). */
    var fadeStart: Float = 0.55f

    /** Где картинка исчезает полностью (0..1). */
    var fadeEnd: Float = 1.0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val xfer = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

    override fun onDraw(canvas: Canvas) {
        val h = height.toFloat()
        val w = width.toFloat()
        if (h <= 0f || w <= 0f) {
            super.onDraw(canvas)
            return
        }
        val save = canvas.saveLayer(0f, 0f, w, h, null)
        super.onDraw(canvas)
        paint.shader = LinearGradient(
            0f, h * fadeStart,
            0f, h * fadeEnd,
            intArrayOf(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0x00FFFFFF),
            floatArrayOf(0f, 0.30f, 1f),
            Shader.TileMode.CLAMP,
        )
        paint.xfermode = xfer
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.xfermode = null
        paint.shader = null
        canvas.restoreToCount(save)
    }
}
