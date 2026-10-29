package su.gwarden.music.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import su.gwarden.music.R
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.Short

/**
 * Страницы ленты шортсов: одна страница = один «лучший кусок» трека.
 */
class ShortAdapter(
    private val onFull: (Short) -> Unit,
    private val onAdd: (Short) -> Unit,
    private val onArtist: (Short) -> Unit = {},
    private val onToggle: (Short) -> Unit = {},
) : RecyclerView.Adapter<ShortAdapter.VH>() {

    /** Лайки и комментарии конкретного шортса (как в TikTok/SoundCloud). */
    data class Social(
        val liked: Boolean = false,
        val likes: Int = 0,
        val comments: Int = 0,
    )

    /** Откуда брать состояние (кэш в активити). */
    var socialOf: (Short) -> Social? = { null }
    var onLike: (Short) -> Unit = {}
    var onComments: (Short) -> Unit = {}

    private val items = ArrayList<Short>()
    private val added = HashSet<String>()

    /** Какой трек сейчас играет и на паузе ли он (для кружка «плей»). */
    private var playingId: String? = null
    private var paused = false

    fun setPlayback(id: String?, isPaused: Boolean) {
        if (playingId == id && paused == isPaused) return
        playingId = id
        paused = isPaused
        notifyDataSetChanged()
    }

    fun submit(list: List<Short>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun append(list: List<Short>) {
        if (list.isEmpty()) return
        val from = items.size
        items.addAll(list)
        notifyItemRangeInserted(from, list.size)
    }

    fun item(i: Int): Short? = items.getOrNull(i)

    /** Трек добавили к себе - плюсик превращается в галочку. */
    fun markAdded(id: String) {
        if (!added.add(id)) return
        val idx = items.indexOfFirst { it.track.id == id }
        if (idx >= 0) notifyItemChanged(idx)
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_short, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val sh = items[position]
        val t = sh.track
        h.title.text = t.title
        h.artist.text = t.artist
        h.prog.played = 0f
        h.full.setOnClickListener { onFull(sh) }
        h.add.setOnClickListener { onAdd(sh) }
        // тап по имени артиста - переход в его карточку (как в плеере)
        h.artist.setOnClickListener { onArtist(sh) }
        // тап по обложке - пауза/продолжить текущий шортс
        h.orb.setOnClickListener { onToggle(sh) }
        h.art.setOnClickListener { onToggle(sh) }
        h.pp.setOnClickListener { onToggle(sh) }
        val isCurrent = playingId != null && playingId == t.id
        h.pp.visibility = if (isCurrent && paused) View.VISIBLE else View.GONE

        val isAdded = t.id in added
        h.addIc.setImageResource(if (isAdded) R.drawable.ic_check else R.drawable.ic_plus)
        h.add.alpha = if (isAdded) 0.45f else 1f
        h.add.isEnabled = !isAdded

        val soc = socialOf(sh)
        h.likeIc.setImageResource(
            if (soc?.liked == true) R.drawable.ic_heart_fill else R.drawable.ic_heart_outline
        )
        h.likeIc.imageTintList = android.content.res.ColorStateList.valueOf(
            if (soc?.liked == true) 0xFFFFFFFF.toInt() else 0xFFD8D8D8.toInt()
        )
        h.likeCount.text = if ((soc?.likes ?: 0) > 0) soc!!.likes.toString() else ""
        h.cmtCount.text = if ((soc?.comments ?: 0) > 0) soc!!.comments.toString() else ""
        h.like.setOnClickListener { onLike(sh) }
        h.cmt.setOnClickListener { onComments(sh) }

        ImageLoader.load(h.art, ApiClient.img(t.artwork, 900), 0, R.drawable.ic_music_note, null)
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val orb: View = v.findViewById(R.id.short_orb)
        val art: ImageView = v.findViewById(R.id.short_art)
        val pp: ImageView = v.findViewById(R.id.short_pp)
        val title: TextView = v.findViewById(R.id.short_title)
        val artist: TextView = v.findViewById(R.id.short_artist)
        val full: TextView = v.findViewById(R.id.short_full)
        val add: View = v.findViewById(R.id.short_add)
        val addIc: ImageView = v.findViewById(R.id.short_add_ic)
        val prog: ClipProgress = v.findViewById(R.id.short_progress)
        val comment: TextView = v.findViewById(R.id.short_comment)
        val like: View = v.findViewById(R.id.short_like)
        val likeIc: ImageView = v.findViewById(R.id.short_like_ic)
        val likeCount: TextView = v.findViewById(R.id.short_like_count)
        val cmt: View = v.findViewById(R.id.short_cmt)
        val cmtCount: TextView = v.findViewById(R.id.short_cmt_count)
    }
}
