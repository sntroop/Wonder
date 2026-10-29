package su.gwarden.music.model

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * Плейлисты ВНУТРИ приложения (свои, не VK).
 *
 * В VK ничего не пишем: владелец просил не трогать его аккаунт. Поэтому
 * плейлист - это просто список треков в filesDir/playlists.json. Треки
 * храним целиком (id/source/обложка/длительность), чтобы плейлист играл
 * и после перезапуска, и с VK, и со SoundCloud.
 */
data class LocalPlaylist(
    val id: String = "",
    val title: String = "",
    val desc: String = "",
    /** своя обложка (url); пусто - берём обложку первого трека */
    val cover: String = "",
    val tracks: MutableList<Track> = mutableListOf(),
)

object LocalPlaylists {

    private val gson = Gson()
    private var file: File? = null
    private val items = mutableListOf<LocalPlaylist>()

    fun reset(ctx: Context, prefix: String) {
        file = null
        items.clear()
        init(ctx, prefix)
    }

    fun init(ctx: Context, prefix: String = "") {
        if (file != null) return
        val suf = if (prefix.isEmpty()) "" else "_$prefix"
        val f = File(ctx.filesDir, "playlists$suf.json")
        file = f
        runCatching {
            if (f.exists() && f.length() > 0) {
                val arr = gson.fromJson(f.readText(), Array<LocalPlaylist>::class.java)
                    ?: emptyArray()
                items.clear()
                arr.filter { it.id.isNotEmpty() }
                    .forEach { pl -> items.add(pl.copy(tracks = pl.tracks.toMutableList())) }
            }
        }
    }

    fun all(): List<LocalPlaylist> = items.toList()

    fun byId(id: String): LocalPlaylist? = items.firstOrNull { it.id == id }

    /** Обложка плейлиста: своя, иначе обложка первого трека, иначе пусто. */
    fun coverOf(pl: LocalPlaylist): String =
        pl.cover.ifEmpty { pl.tracks.firstOrNull()?.artwork.orEmpty() }

    fun create(
        title: String,
        desc: String,
        cover: String,
        tracks: List<Track>,
    ): LocalPlaylist {
        val pl = LocalPlaylist(
            id = "loc" + System.currentTimeMillis(),
            title = title.trim().ifEmpty { "Мой плейлист" },
            desc = desc.trim(),
            cover = cover.trim(),
            tracks = tracks.distinctBy { it.source + "|" + it.id }.toMutableList(),
        )
        items.add(0, pl)
        save()
        return pl
    }

    /** true - добавили, false - такой трек уже есть. */
    fun addTrack(plId: String, t: Track): Boolean {
        val pl = byId(plId) ?: return false
        if (pl.tracks.any { it.id == t.id && it.source == t.source }) return false
        pl.tracks.add(t)
        save()
        return true
    }

    fun removeTrack(plId: String, t: Track) {
        val pl = byId(plId) ?: return
        pl.tracks.removeAll { it.id == t.id && it.source == t.source }
        save()
    }

    /** Своя обложка плейлиста (путь к файлу в filesDir/covers). */
    fun setCover(plId: String, path: String) {
        val i = items.indexOfFirst { it.id == plId }
        if (i < 0) return
        items[i] = items[i].copy(cover = path)
        save()
    }

    fun delete(plId: String) {
        items.removeAll { it.id == plId }
        save()
    }

    fun count(t: Track): Int = items.count { pl ->
        pl.tracks.any { it.id == t.id && it.source == t.source }
    }

    fun save() {
        runCatching {
            file?.writeText(gson.toJson(items.toList()))
        }
    }
}
