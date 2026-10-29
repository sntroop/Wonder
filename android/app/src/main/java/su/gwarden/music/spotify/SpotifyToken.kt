package su.gwarden.music.spotify

import android.content.Context

/**
 * Публичная сборка Wonder: получение и хранение Spotify-токена отключено.
 */
object SpotifyToken {

    fun spDc(ctx: Context): String = ""

    fun saveSpDc(ctx: Context, value: String) { }

    fun clear(ctx: Context) { }

    fun fetch(value: String, cb: (String?, Long, String?) -> Unit) {
        cb(null, 0L, "недоступно в публичной сборке")
    }
}
