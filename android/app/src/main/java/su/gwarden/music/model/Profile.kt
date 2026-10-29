package su.gwarden.music.model

import com.google.gson.annotations.SerializedName

/** Статистика автора: треки, прослушивания, лайки и подписки «как автора». */
data class ProfileStats(
    @SerializedName("tracks") val tracks: Int = 0,
    @SerializedName("plays") val plays: Int = 0,
    @SerializedName("likes") val likes: Int = 0,
    @SerializedName("subs") val subs: Int = 0,
)

/** Свой трек в профиле (source = own). */
data class ProfileTrack(
    @SerializedName("id") val id: String = "",
    @SerializedName("source") val source: String = "own",
    @SerializedName("title") val title: String = "",
    @SerializedName("artist") val artist: String = "",
    @SerializedName("duration") val duration: Int = 0,
    @SerializedName("artwork") val artwork: String? = null,
    @SerializedName("plays") val plays: Int = 0,
    @SerializedName("likes") val likes: Int = 0,
    @SerializedName("date") val date: Long = 0L,
    @SerializedName("genre") val genre: String = "",
)

/** Профиль: свой (self = true) или публичный профиль автора. */
data class ProfileInfo(
    @SerializedName("uid") val uid: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("bio") val bio: String = "",
    @SerializedName("avatar") val avatar: String = "",
    @SerializedName("self") val self: Boolean = false,
    @SerializedName("can_upload_in") val canUploadIn: Int = 0,
    @SerializedName("stats") val stats: ProfileStats = ProfileStats(),
    @SerializedName("tracks") val tracks: List<ProfileTrack> = emptyList(),
    @SerializedName("subscribed") val subscribed: Boolean = false,
)
