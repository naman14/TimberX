package com.naman14.timberx.ui.artist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemAlbumBinding
import com.naman14.timberx.databinding.ItemArtistBinding
import com.naman14.timberx.databinding.ItemPlaylistBinding
import com.naman14.timberx.databinding.ItemSongsBinding
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.vo.Album
import com.naman14.timberx.vo.Artist
import com.naman14.timberx.vo.Playlist
import com.naman14.timberx.vo.Song

class ArtistAdapter: RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {

    var artists: ArrayList<Artist>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemArtistBinding>(LayoutInflater.from(parent.context),
                R.layout.item_artist, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(artists!![position])
    }

    override fun getItemCount(): Int {
        return artists?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.artist = artist
            binding.executePendingBindings()
        }
    }

    fun updateData(artists: ArrayList<Artist>) {
        this.artists = artists
        notifyDataSetChanged()
    }
}