package su.gwarden.music.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import su.gwarden.music.R
import su.gwarden.music.api.ApiClient
import su.gwarden.music.model.LocalLibrary
import su.gwarden.music.model.Track
import su.gwarden.music.vk.VkLocal
import su.gwarden.music.ui.EqBars

class TrackAdapter(
    private val onPlay: (Track) -> Unit,
    private val onAdd: ((Track) -> Unit)? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Track>()
    private var playingId: String? = null
    private var header: View? = null
    private var allowAdd = true
    private val added = HashSet<String>()

    /** id треков, которые уже в «Моей музыке» (VK-библиотека + локальные) - плюс им не нужен. */
    private var mineIds: Set<String> = emptySet()

    /** id треков с пустым названием (логируем один раз на трек). */
    private val emptyMeta = HashSet<String>()

    /** Режим «Моя музыка»: разрешаем таскать и свайпать локальные треки. */
    var editable = false

    /** Режим «Лайки»: вместо плюсика рисуем залитое сердечко. */
    var likedMode = false
        set(value) {
            if (field == value) return
            field = value
            notifyDataSetChanged()
        }

    /** Играет ли сейчас (чтобы эквалайзер в строке не «дышал» на паузе). */
    private var isPlayingNow = false

    fun setPlayingState(p: Boolean) {
        if (isPlayingNow == p) return
        isPlayingNow = p
        // точечно по играющей строке: notifyDataSetChanged сбрасывает позиции
        // и подсветка-капля на кадр пропадает
        val pos = playingPosition()
        if (pos >= 0) notifyItemChanged(pos)
    }

    /** Куда переехала подсветка играющего трека (MainActivity двигает каплю). */
    var onPlayingMoved: (() -> Unit)? = null

    /** Нажали «три точки» у трека. */
    var onMenu: ((Track, View) -> Unit)? = null

    fun submit(list: List<Track>, allowAdd: Boolean = true) {
        items.clear()
        items.addAll(list)
        header = null
        this.allowAdd = allowAdd
        notifyDataSetChanged()
        onPlayingMoved?.invoke()
    }

    /** Трек уже добавлен в «Мою музыку» - плюс больше не показываем. */
    fun setMineIds(ids: Set<String>) {
        mineIds = HashSet(ids)
        notifyDataSetChanged()
    }

    fun markAdded(id: String) {
        added.add(id)
        notifyDataSetChanged()
    }

    /** Шапка списка (например, обложка и описание плейлиста). */
    fun setHeader(view: View?) {
        header = view
        notifyDataSetChanged()
    }

    fun currentQueue(): List<Track> = items.toList()

    fun setPlayingId(id: String?) {
        // ВАЖНО: НЕ notifyDataSetChanged. После него RecyclerView помечает
        // данные изменёнными (mDataSetHasChangedAfterLayout) и
        // findViewHolderForAdapterPosition() возвращает null, пока не пройдёт
        // layout - из-за этого капля пряталась вместо перелёта.
        val old = playingPosition()
        playingId = id
        val new = playingPosition()
        if (old >= 0) notifyItemChanged(old)
        if (new >= 0 && new != old) notifyItemChanged(new)
        onPlayingMoved?.invoke()
    }

    /** Позиция играющего трека в адаптере (-1 если нет). */
    fun playingPosition(): Int {
        val id = playingId ?: return -1
        val i = items.indexOfFirst { it.id == id }
        if (i < 0) return -1
        return i + if (hasHeader) 1 else 0
    }

    fun nextInQueue(): Track? {
        if (items.isEmpty()) return null
        val idx = items.indexOfFirst { it.id == playingId }
        return if (idx >= 0 && idx + 1 < items.size) items[idx + 1] else items.firstOrNull()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val add: android.widget.ImageButton = view.findViewById(R.id.item_add)
        val scrim: View = view.findViewById(R.id.item_scrim)
        val eq: EqBars = view.findViewById(R.id.item_eq)
        val down: View = view.findViewById(R.id.item_down)
        val more: View = view.findViewById(R.id.item_more)
        val title: TextView = view.findViewById(R.id.item_title)
        val artist: TextView = view.findViewById(R.id.item_artist)
        val duration: TextView = view.findViewById(R.id.item_duration)
        val art: ImageView = view.findViewById(R.id.item_art)
    }

    class HeaderVH(view: View) : RecyclerView.ViewHolder(view)

    private val hasHeader: Boolean get() = header != null

    override fun getItemCount(): Int = items.size + if (hasHeader) 1 else 0

    override fun getItemViewType(position: Int): Int =
        if (hasHeader && position == 0) TYPE_HEADER else TYPE_TRACK

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) return HeaderVH(header!!)
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is VH) return
        val track = items[position - if (hasHeader) 1 else 0]
        // Пустое название (битый объект VK/SC) раньше давало строку-скелет без
        // текста, которая при этом играла. Показываем заглушку и один раз
        // пишем в лог, ЧТО это был за трек, чтобы найти источник.
        holder.title.text = track.title.ifBlank { "Без названия" }
        holder.artist.text = track.artist
        if (track.title.isBlank() && track.id.isNotEmpty() && emptyMeta.add(track.id)) {
            ApiClient.log("meta_empty", "${track.source}:${track.id} dur=${track.duration}")
        }
        val blocked = track.playable == false && !(track.source == "vk" && VkLocal.ready)
        holder.duration.text = if (blocked) "—" else fmt(track.duration)
        holder.itemView.alpha = if (blocked) 0.45f else 1f
        ImageLoader.load(
            holder.art, ApiClient.img(track.artwork, 300), 0, R.drawable.ic_music_note
        )

        val canAdd = allowAdd && onAdd != null && track.id.isNotEmpty() &&
            !added.contains(track.id) && !track.mine &&
            track.id !in mineIds && !LocalLibrary.contains(track)
        if (likedMode) {
            // раздел «Лайки»: плюсик тут бессмысленен, показываем сердечко
            holder.add.visibility = View.VISIBLE
            holder.add.setImageResource(R.drawable.ic_heart_fill)
            holder.add.isClickable = false
            holder.add.isEnabled = false
            holder.add.alpha = 1f
        } else {
            holder.add.isEnabled = true
            holder.add.setImageResource(R.drawable.ic_plus)
            // ГРАБЛЯ: если прятать плюсик через GONE, соседняя кнопка «⋮»
            // прыгает вправо прямо под пальцем. Держим место занятым (INVISIBLE).
            holder.add.visibility = if (canAdd) View.VISIBLE else View.INVISIBLE
            holder.add.isClickable = canAdd
            holder.add.setOnClickListener { onAdd?.invoke(track) }
        }
        holder.more.setOnClickListener { onMenu?.invoke(track, it) }

        // скачан на телефон - играет без интернета
        holder.down.visibility =
            if (su.gwarden.music.offline.Offline.isDownloaded(track)) View.VISIBLE else View.GONE

        val playing = track.id == playingId
        // подсветку играющего трека рисует LiquidBlob ПОД списком (она перетекает)
        holder.itemView.setBackgroundResource(R.drawable.bg_row_ripple)

        // обложка играющего: затемняем и показываем мини-эквалайзер
        if (playing) {
            holder.scrim.visibility = View.VISIBLE
            holder.eq.visibility = View.VISIBLE
            if (isPlayingNow) holder.eq.start() else holder.eq.stop()
        } else {
            holder.eq.stop()
            holder.eq.visibility = View.GONE
            holder.scrim.visibility = View.GONE
        }
        holder.title.setTextColor(
            if (playing) 0xFFFFFFFF.toInt() else 0xFFE6E6E6.toInt()
        )
        holder.itemView.setOnClickListener { onPlay(track) }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is VH) {
            holder.eq.stop()
            holder.eq.visibility = View.GONE
            holder.scrim.visibility = View.GONE
        }
        super.onViewRecycled(holder)
    }

    // ---- drag&drop / удаление ----

    fun trackAt(adapterPos: Int): Track? {
        val i = adapterPos - if (hasHeader) 1 else 0
        return items.getOrNull(i)
    }

    private fun localCount(): Int = items.count { LocalLibrary.contains(it) }

    /** Перемещение любого трека (VK-библиотеку тоже разрешаем таскать). */
    fun moveItem(fromPos: Int, toPos: Int): Boolean {
        val h = if (hasHeader) 1 else 0
        val from = fromPos - h
        val to = toPos - h
        if (from < 0 || to < 0 || from >= items.size || to >= items.size) return false
        if (from == to) return false
        val t = items.removeAt(from)
        items.add(to, t)
        notifyItemMoved(from + h, to + h)
        return true
    }

    /** Удалить конкретный трек из списка. */
    fun removeTrack(track: Track) {
        val i = items.indexOfFirst { it.id == track.id && it.source == track.source }
        if (i < 0) return
        items.removeAt(i)
        notifyItemRemoved(i + headerOffset())
    }

    private fun headerOffset() = if (hasHeader) 1 else 0

    fun localOrder(): List<Track> = items.filter { LocalLibrary.contains(it) }

    fun removeAt(adapterPos: Int) {
        val i = adapterPos - if (hasHeader) 1 else 0
        if (i < 0 || i >= items.size) return
        items.removeAt(i)
        notifyItemRemoved(adapterPos)
    }

    private fun fmt(sec: Int): String {
        if (sec <= 0) return "--:--"
        return "%d:%02d".format(sec / 60, sec % 60)
    }

    private companion object {
        const val TYPE_TRACK = 0
        const val TYPE_HEADER = 1
    }
}
