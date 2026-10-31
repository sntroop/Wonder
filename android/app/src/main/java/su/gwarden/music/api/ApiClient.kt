package su.gwarden.music.api

import android.content.Context
import java.io.IOException
import su.gwarden.music.model.HomeBlock
import su.gwarden.music.model.Track
import su.gwarden.music.model.VkPlaylist

/**
 * Публичная сборка Wonder.
 *
 * Сетевой слой здесь отключён: нет адресов сервера, ключей и токенов.
 * Методы оставлены только для сборки UI;
 * они ничего не отправляют и сразу возвращают пустой/ошибочный результат.
 */
object ApiClient {

    var baseUrl: String = ""
    var token: String = ""
    var userId: Int = 0
    // Публичная сборка: показываем основной интерфейс без сетевого входа.
    val loggedIn: Boolean get() = true
    var lang: String = "ru"
    var onAuthExpired: (() -> Unit)? = null
    var onServerWarning: ((String) -> Unit)? = null

    fun init(context: Context) { }

    fun streamUrl(source: String, id: String, direct: Boolean = false): String {
        return ""
    }

    fun img(url: String?, size: Int = 0): String? {
        return url
    }

    fun logSync(ev: String, msg: String = "") { }

    fun log(ev: String, msg: String = "") { }

    fun feedback(ev: String, source: String, id: String, artist: String,
                 playedMs: Int, posMs: Int = 0, durMs: Int = 0,
                 cov: Float = 0f, src: String = "", hour: Int = -1) { }

    fun recent(t: su.gwarden.music.model.Track) { }

    fun recentTracks(done: java.util.function.Consumer<List<su.gwarden.music.model.Track>>) { }

    fun vkAdd(trackId: String, accessKey: String, onResult: (Result<Unit>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun scLike(trackId: String, onResult: (Result<Unit>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun home(onResult: (Result<List<HomeBlock>>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun homeRefresh(onResult: (Result<List<HomeBlock>>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun search(query: String, source: String, onResult: (Result<List<su.gwarden.music.model.Track>>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun myAudioCount(onDone: (Int) -> Unit) { }

    fun myPlaylistsCount(onDone: (Int) -> Unit) { }

    fun myAudio(count: Int = 500, onResult: (Result<List<su.gwarden.music.model.Track>>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun followPlaylist(
        ownerId: String,
        playlistId: String,
        accessKey: String,
        onResult: (Result<String>) -> Unit,
    ) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun myPlaylists(onResult: (Result<List<VkPlaylist>>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun prefetch(items: List<su.gwarden.music.model.Track>, source: String) { }

    fun playlistTracks(
        ownerId: String,
        playlistId: String,
        accessKey: String,
        onResult: (Result<List<su.gwarden.music.model.Track>>) -> Unit,
    ) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun importPlaylist(
        url: String,
        target: String = "vk",
        limit: Int = 40,
        onResult: (Result<List<su.gwarden.music.model.Track>>) -> Unit,
    ) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    data class ImportStart(
        val job: String = "",
        val title: String = "",
        val cover: String = "",
        val source: String = "",
        val total: Int = 0,
    )

    data class ImportStatus(
        val state: String = "",
        val done: Int = 0,
        val total: Int = 0,
        val matched: Int = 0,
        val title: String = "",
        val cover: String = "",
        val source: String = "",
        val error: String = "",
        val items: List<su.gwarden.music.model.Track> = emptyList(),
    )

    data class SpotifyStatus(
        val configured: Boolean = false,
        val connected: Boolean = false,
        val name: String = "",
        val redirect: String = "",
        @com.google.gson.annotations.SerializedName("expires_at")
        val expiresAt: Long = 0L,
    )

    data class SpotifyPlaylist(
        val id: String = "",
        val title: String = "",
        val cover: String = "",
        val count: Int = 0,
        val owner: String = "",
    )

    fun importJob(url: String, limit: Int = 60, onDone: (ImportStart?, String?) -> Unit) { }

    fun importJobStatus(job: String, onDone: (ImportStatus?, String?) -> Unit) { }

    fun pushSpotifyToken(token: String, expiresAtMs: Long, onDone: (Boolean, String?) -> Unit) { }

    fun spotifyLogout(onDone: (Boolean, String?) -> Unit) { }

    data class YaStatus(
        val connected: Boolean = false,
        val name: String = "",
    )

    data class ImportSource(
        val connected: Boolean = false,
        val imported: Int = 0,
        val name: String = "",
    )

    data class ImportSources(
        val vk: ImportSource = ImportSource(),
        val soundcloud: ImportSource = ImportSource(),
        val spotify: ImportSource = ImportSource(),
        val yandex: ImportSource = ImportSource(),
    )

    fun importSources(onDone: (ImportSources?, String?) -> Unit) { }

    fun scImport(onDone: (Int, String?) -> Unit) { }

    fun yandexStatus(onDone: (YaStatus?, String?) -> Unit) { }

    fun yandexLogout(onDone: (Boolean, String?) -> Unit) { }

    fun yandexPlaylists(onDone: (List<SpotifyPlaylist>?, String?) -> Unit) { }

    fun yandexImport(
        kind: String,
        id: String,
        limit: Int = 60,
        onDone: (ImportStart?, String?) -> Unit,
    ) { }

    fun spotifyStatus(onDone: (SpotifyStatus?, String?) -> Unit) { }

    fun spotifyAuthUrl(onDone: (String?, String?) -> Unit) { }

    fun spotifySetApp(clientId: String, clientSecret: String, onDone: (String?, String?) -> Unit) { }

    fun spotifyPlaylists(onDone: (List<SpotifyPlaylist>?, String?) -> Unit) { }

    fun spotifyImport(
        kind: String,
        id: String,
        limit: Int = 60,
        onDone: (ImportStart?, String?) -> Unit,
    ) { }

    data class SearchAll(
        val tracks: List<su.gwarden.music.model.Track> = emptyList(),
        val artists: List<su.gwarden.music.ui.RowItem> = emptyList(),
        val albums: List<su.gwarden.music.ui.RowItem> = emptyList(),
        val playlists: List<su.gwarden.music.ui.RowItem> = emptyList(),
        val query: String = "",
    )

    fun searchAll(query: String, onDone: (SearchAll?, String?) -> Unit) { }

    fun artistWave(
        source: String,
        id: String,
        name: String,
        limit: Int = 40,
        onDone: (List<su.gwarden.music.model.Track>?, String?) -> Unit,
    ) { }

    data class ArtistAlbum(
        val id: String = "",
        val title: String = "",
        val year: Int = 0,
        val cover: String = "",
        val count: Int = 0,
    )

    data class ArtistPage(
        val source: String = "",
        val id: String = "",
        val name: String = "",
        val cover: String = "",
        val albums: List<ArtistAlbum> = emptyList(),
        val last: ArtistAlbum? = null,
        val tracks: List<su.gwarden.music.model.Track> = emptyList(),
        @com.google.gson.annotations.SerializedName("in_library")
        val inLibrary: Boolean = false,
    )

    data class LibraryArtist(
        val source: String = "",
        val id: String = "",
        val name: String = "",
        val cover: String = "",
    )

    data class LibraryStats(
        val tracks: Int = 0,
        val artists: Int = 0,
        val playlists: Int = 0,
    )

    fun libraryStats(onDone: (LibraryStats?) -> Unit) { }

    fun artists(onDone: (List<LibraryArtist>?, String?) -> Unit) { }

    fun artistAdd(source: String, id: String, name: String, cover: String,
                  onDone: (Boolean, String?) -> Unit) { }

    fun artistRemove(source: String, id: String, name: String,
                     onDone: (Boolean, String?) -> Unit) { }

    fun artistInLibrary(name: String, source: String, id: String,
                        onDone: (Boolean) -> Unit) { }

    fun artistPage(
        source: String,
        id: String,
        name: String,
        limit: Int = 30,
        onDone: (ArtistPage?, String?) -> Unit,
    ) { }

    data class OpenedCollection(
        val ready: Boolean = false,
        val job: String = "",
        val title: String = "",
        val cover: String = "",
        val source: String = "",
        val total: Int = 0,
        val items: List<su.gwarden.music.model.Track> = emptyList(),
    )

    fun openCollection(
        source: String,
        kind: String,
        id: String,
        limit: Int = 60,
        onDone: (OpenedCollection?, String?) -> Unit,
    ) { }

    data class BeatMap(
        val rate: Float,
        val energy: FloatArray,
        val beats: FloatArray = FloatArray(0),
        val bands: String = "",
    )

    fun shorts(limit: Int = 10, refresh: Boolean = false,
               exclude: List<String> = emptyList(),
               onResult: (Result<List<su.gwarden.music.model.Short>>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun beat(source: String, id: String, onResult: (Result<BeatMap>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun wave(limit: Int = 40, refresh: Boolean = false, onResult: (Result<List<su.gwarden.music.model.Track>>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun similar(
        seed: String,
        artist: String = "",
        title: String = "",
        limit: Int = 30,
        onResult: (Result<List<su.gwarden.music.model.Track>>) -> Unit,
    ) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun remixes(
        artist: String,
        title: String,
        limit: Int = 80,
        onResult: (Result<List<su.gwarden.music.model.Track>>) -> Unit,
    ) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun replace(
        artist: String,
        title: String,
        duration: Int,
        onResult: (su.gwarden.music.model.Track?) -> Unit,
    ) { }

    fun fixStatus(onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun fixBlocked(force: Boolean = false, onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun vkStatus(onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun register(username: String, password: String, onDone: (Boolean, String?) -> Unit) { }

    fun loginPassword(username: String, password: String, onDone: (Boolean, String?) -> Unit) { }

    fun usernameFree(name: String, onDone: (Boolean, String?) -> Unit) { }

    fun guestLogin(onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun libraryAdd(t: su.gwarden.music.model.Track,
                   onDone: ((Boolean) -> Unit)? = null) { }

    fun libraryRemove(source: String, id: String, onDone: ((Boolean) -> Unit)? = null) { }

    fun interestsCached(): List<su.gwarden.music.model.InterestGroup>? {
        return null
    }

    fun interests(onResult: (List<su.gwarden.music.model.InterestGroup>?) -> Unit) { }

    fun saveGuestPrefs(genres: List<String>, artists: List<String>,
                       onDone: (Boolean, String?) -> Unit) { }

    fun guestPrefs(onDone: (List<String>?, List<String>?) -> Unit) { }

    fun authStart(onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun authCheck(id: String, onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun scMe(onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun authMe(onResult: (Result<String>) -> Unit) {
        onResult(Result.failure(IOException("Сетевой слой отключён в публичной сборке")))
    }

    fun authMeText(cb: (String?) -> Unit) {
        cb(null)
    }

    fun logout() { }

    data class SocialComment(
        val id: Long = 0, val uid: Long = 0, val name: String = "",
        val pos: Long = 0, val text: String = "", val mine: Boolean = false,
    )

    data class SocialInfo(
        val likes: Int = 0, val liked: Boolean = false,
        val comments: List<SocialComment> = emptyList(),
    )

    data class LyricLine(val t: Long = 0, val text: String = "")

    data class LyricsData(
        val found: Boolean = false, val synced: Boolean = false,
        val lines: List<LyricLine> = emptyList(), val plain: String = "",
    )

    fun trackSocial(source: String, id: String, onDone: (SocialInfo?) -> Unit) { }

    fun toggleLike(t: Track, onDone: (Int, Boolean) -> Unit) { }

    fun likeState(t: Track, onDone: (Boolean) -> Unit) { onDone(false) }

    fun toggleLikeSimple(t: Track, onDone: (Boolean) -> Unit) { onDone(false) }

    fun likes(onDone: (List<Track>) -> Unit) { onDone(emptyList()) }

    fun googleLogin(idToken: String, onDone: (Boolean, String?) -> Unit) {
        onDone(false, "Сетевой слой отключён в публичной сборке")
    }

    fun addComment(t: Track, posMs: Int, text: String, onDone: (Boolean) -> Unit) { }

    fun lyrics(
        artist: String, title: String, duration: Int, source: String, id: String,
        onDone: (LyricsData?) -> Unit,
    ) { }

    fun appPlaylists(onDone: (List<su.gwarden.music.model.VkPlaylist>) -> Unit) { }

    fun createPlaylist(
        title: String, description: String,
        onDone: (su.gwarden.music.model.VkPlaylist?) -> Unit,
    ) { }

    fun addToAppPlaylist(pid: String, t: Track, onDone: (Boolean) -> Unit) { }

    fun appPlaylistTracks(pid: String, onDone: (List<Track>) -> Unit) { }

    fun profileInfo(userId: Int = 0, onResult: (su.gwarden.music.model.ProfileInfo?) -> Unit) { }

    fun profile(userId: Int = 0, onResult: (String?) -> Unit) { }

    fun profileUpdate(name: String, bio: String, onResult: (String?, String?) -> Unit) { }

    fun uploadAvatar(uri: android.net.Uri, onResult: (String?, String?) -> Unit) { }

    fun uploadTrack(
        audio: android.net.Uri,
        cover: android.net.Uri?,
        title: String,
        genre: String,
        dropStart: Float,
        dropLen: Float,
        duration: Int,
        onResult: (String?, String?) -> Unit,
    ) { }

    fun ownPlay(id: String) { }

    fun authorSub(author: Int, on: Boolean, onResult: (Int?) -> Unit) { }

    fun shortOf(
        source: String,
        id: String,
        onResult: (List<su.gwarden.music.model.Short>?) -> Unit,
    ) { }

    fun ownDelete(id: String, onResult: (Boolean, String?) -> Unit) { }

    // --- импорт: в публичной сборке сети нет ---
    fun importTikTok(url: String, onDone: (ImportStart?, String?) -> Unit) {
        onDone(null, "Сетевой слой отключён в публичной сборке")
    }

    fun ocrScreenshot(uri: android.net.Uri, onDone: (String?, String?) -> Unit) {
        onDone(null, "Сетевой слой отключён в публичной сборке")
    }

    fun importText(
        text: String,
        title: String = "Импорт по тексту",
        onDone: (ImportStart?, String?) -> Unit,
    ) {
        onDone(null, "Сетевой слой отключён в публичной сборке")
    }

}
