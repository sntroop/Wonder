package su.gwarden.music.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import su.gwarden.music.api.ApiClient
import su.gwarden.music.R
import su.gwarden.music.model.VkPlaylist

class PlaylistAdapter(
    private val onOpen: (VkPlaylist) -> Unit,
) : RecyclerView.Adapter<PlaylistAdapter.VH>() {

    private val items = mutableListOf<VkPlaylist>()

    fun submit(list: List<VkPlaylist>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.item_title)
        val artist: TextView = view.findViewById(R.id.item_artist)
        val art: ImageView = view.findViewById(R.id.item_art)
        val duration: TextView = view.findViewById(R.id.item_duration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pl = items[position]
        holder.title.text = pl.title
        holder.artist.text = "${pl.count} треков"
        holder.duration.text = ""
        val seed = pl.id + pl.ownerId + pl.title
        if (pl.art) {
            holder.art.setImageBitmap(PlaylistArt.bitmap(holder.art.context, seed, pl.title, 192))
        } else {
            PlaylistArt.into(holder.art, pl.cover, seed, pl.title, 192)
        }
        holder.itemView.setOnClickListener { onOpen(pl) }
    }
}
