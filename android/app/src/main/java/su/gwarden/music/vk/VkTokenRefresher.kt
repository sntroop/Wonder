package su.gwarden.music.vk

import android.content.Context

/**
 * Публичная сборка Wonder: автоматическое обновление VK-токена отключено.
 */
object VkTokenRefresher {

    fun refresh(ctx: Context, onDone: (Boolean) -> Unit) {
        onDone(false)
    }
}
