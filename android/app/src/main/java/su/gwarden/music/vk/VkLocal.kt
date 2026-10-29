package su.gwarden.music.vk

import android.content.Context

/**
 * Публичная сборка Wonder.
 *
 * Локального VK-клиента здесь нет.
 */
object VkLocal {

    @Volatile
    var ready: Boolean = false
        private set

    @Volatile
    var status: String = "недоступно в публичной сборке"

    fun init(ctx: Context) { }

    fun ensure(ctx: Context, onDone: ((Boolean) -> Unit)? = null) {
        onDone?.invoke(false)
    }

    fun urlFor(trackId: String, accessKey: String = ""): String? = null

    fun canPlay(track: su.gwarden.music.model.Track): Boolean = false
}
