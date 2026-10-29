package su.gwarden.music.vk

import android.content.Context

/**
 * Публичная сборка Wonder.
 *
 * Прямых запросов к VK и хранения VK-токенов здесь нет.
 */
object VkApi {

    var token: String = ""

    var onTokenRejected: (() -> Unit)? = null

    fun init(ctx: Context) { }

    fun hasToken(): Boolean = false

    fun checkToken(t: String): Boolean = false

    fun me(): Triple<Int, String, String>? = null

    fun userId(): String = ""

    fun myPlaylists(count: Int = 50): List<su.gwarden.music.model.VkPlaylist> = emptyList()

    fun playlistTracks(
        owner: String,
        playlistId: String,
        accessKey: String,
        count: Int = 100,
    ): List<su.gwarden.music.model.Track> = emptyList()

    fun addToPlaylist(
        ownerId: String,
        playlistId: String,
        audioId: String,
        accessKey: String = "",
    ): Boolean = false

    fun trackUrl(trackId: String, accessKey: String = ""): String = ""

    fun search(query: String, count: Int = 60): List<su.gwarden.music.model.Track> = emptyList()
}
