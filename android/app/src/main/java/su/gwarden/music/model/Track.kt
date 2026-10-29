package su.gwarden.music.model

import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("id") val id: String = "",
    @SerializedName("source") val source: String = "soundcloud",
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("artwork") val artwork: String? = null,
    @SerializedName("permalink") val permalink: String = "",
    /** false = VK ограничил трек по региону, играть нельзя */
    @SerializedName("playable") val playable: Boolean? = null,
    /** исходный VK-трек, если этот подменён (SoundCloud/реаплоад) */
    @SerializedName("vk_id") val vkId: String? = null,
    /** получено локальным VK на телефоне - играть сразу через телефон */
    val local: Boolean = false,
    /** access_key чужого VK-трека (нужен audio.getById на телефоне) */
    @SerializedName("access_key") val accessKey: String = "",
    /** автор плейлиста («Слушайте друг друга») */
    @SerializedName("author") val author: String = "",
    /** совпадение вкуса в процентах (0 = нет данных) */
    @SerializedName("match") val match: Int = 0,
    @SerializedName("description") val description: String = "",
    /** true - обложку рисуем сами (подборки дня), а не берём из VK */
    @SerializedName("art") val art: Boolean = false,
    /** трек уже есть в библиотеке пользователя (плюсик не нужен) */
    @SerializedName("mine") val mine: Boolean = false,
    /** когда добавлен/лайкнут (unix) - для сортировки «Новые / Старые» */
    @SerializedName("date") val date: Long = 0L,
    /** настоящий лайк SoundCloud (а не замена регионально забаненного VK-трека) */
    @SerializedName("sc_like") val scLike: Boolean = false,
    /** трек подменён на другую версию (региональный бан) */
    @SerializedName("substituted") val substituted: Boolean = false,
    /** темп для замены slowed/speed-up (1.0 = обычный) */
    @SerializedName("speed") val speed: Float = 1f,
    /** id автора (свои треки): открыть его профиль */
    @SerializedName("owner") val owner: Int = 0,
)

data class SearchResponse(
    @SerializedName("source") val source: String = "",
    @SerializedName("items") val items: List<Track> = emptyList(),
)

data class VkPlaylist(
    @SerializedName("id") val id: String = "",
    @SerializedName("owner_id") val ownerId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("count") val count: Int = 0,
    @SerializedName("cover") val cover: String = "",
    @SerializedName("access_key") val accessKey: String = "",
    /** автор плейлиста («Слушайте друг друга») */
    @SerializedName("author") val author: String = "",
    /** совпадение вкуса в процентах (0 = нет данных) */
    @SerializedName("match") val match: Int = 0,
    @SerializedName("description") val description: String = "",
    /** true - обложку рисуем сами (подборки дня), а не берём из VK */
    @SerializedName("art") val art: Boolean = false,
    /** трек уже есть в библиотеке пользователя (плюсик не нужен) */
    @SerializedName("mine") val mine: Boolean = false,
    /** vk | soundcloud | wonder - плейлист создан в нашем приложении */
    @SerializedName("source") val source: String = "vk",
)

data class MyPlaylistsResponse(
    @SerializedName("items") val items: List<VkPlaylist> = emptyList(),
)

/** Блок ленты на главной: подборка VK с треками или плейлистами. */
data class HomeBlock(
    @SerializedName("key") val key: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("kind") val kind: String = "tracks",
    @SerializedName("tracks") val tracks: List<Track> = emptyList(),
    @SerializedName("playlists") val playlists: List<VkPlaylist> = emptyList(),
)

data class HomeResponse(
    @SerializedName("blocks") val blocks: List<HomeBlock> = emptyList(),
)

data class RecentResponse(
    @SerializedName("items") val items: List<Track> = emptyList(),
)
