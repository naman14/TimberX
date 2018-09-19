package com.naman14.timberx.ui.albums

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemAlbumBinding
import com.naman14.timberx.databinding.ItemSongsBinding
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.vo.Album
import com.naman14.timberx.vo.Song

class AlbumAdapter: RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    var albums: ArrayList<Album>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemAlbumBinding>(LayoutInflater.from(parent.context),
                R.layout.item_album, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(albums!![position])
    }

    override fun getItemCount(): Int {
        return albums?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.albumArt.clipToOutline = true
            binding.album = album
            binding.executePendingBindings()
        }
    }

    fun updateData(albums: ArrayList<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }
}