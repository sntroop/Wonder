package su.gwarden.music.vk

import android.content.Context

/**
 * Публичная сборка Wonder: автоматическое обновление токена SoundCloud
 * отключено.
 */
object ScTokenRefresher {

    fun refresh(ctx: Context, force: Boolean = false, onDone: (Boolean) -> Unit = {}) {
        onDone(false)
    }
}
