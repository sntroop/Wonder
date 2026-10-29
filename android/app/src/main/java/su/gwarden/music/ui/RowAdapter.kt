package su.gwarden.music.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import su.gwarden.music.R

/**
 * Строка результата поиска: артист / альбом / плейлист.
 * Отдельный адаптер, чтобы не ломать список треков (TrackAdapter).
 */
data class RowItem(
    val kind: String = "",
    val source: String = "",
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val cover: String = "",
)

class RowAdapter(
    private val onClick: (RowItem) -> Unit,
) : RecyclerView.Adapter<RowAdapter.VH>() {

    private val items = mutableListOf<RowItem>()

    fun submit(list: List<RowItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = items[position]
        holder.title.text = row.title
        holder.sub.text = row.subtitle
        val ctx = holder.cover.context
        if (row.cover.isNotEmpty()) {
            holder.cover.visibility = View.VISIBLE
            val req = Glide.with(ctx).load(row.cover)
            if (row.kind == "artist") {
                req.circleCrop().into(holder.cover)
            } else {
                req.centerCrop().into(holder.cover)
            }
        } else {
            holder.cover.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onClick(row) }
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cover: ImageView = v.findViewById(R.id.row_cover)
        val title: TextView = v.findViewById(R.id.row_title)
        val sub: TextView = v.findViewById(R.id.row_sub)
    }
}
