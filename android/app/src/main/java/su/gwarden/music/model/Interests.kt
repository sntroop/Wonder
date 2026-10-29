package su.gwarden.music.model

import com.google.gson.annotations.SerializedName

/** Жанр-интерес: id используется как семя волны, img - обложка карточки. */
data class InterestItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("img") val img: String = "",
)

/** Группа жанров («Электроника», «Рэп и хип-хоп», ...). */
data class InterestGroup(
    @SerializedName("key") val key: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("items") val items: List<InterestItem> = emptyList(),
)

data class InterestsResponse(
    @SerializedName("groups") val groups: List<InterestGroup> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("max") val max: Int = 8,
)
