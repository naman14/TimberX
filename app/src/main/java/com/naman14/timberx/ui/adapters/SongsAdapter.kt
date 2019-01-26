package com.naman14.timberx.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemSongsBinding
import com.naman14.timberx.databinding.ItemSongsHeaderBinding
import com.naman14.timberx.models.Song

class SongsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var songs: List<Song>? = null
    var showHeader = false

    private val typeSongHeader = 0
    private val typeSongItem = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            typeSongHeader -> HeaderViewHolder(DataBindingUtil.inflate<ItemSongsHeaderBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_songs_header, parent, false))
            typeSongItem -> ViewHolder(DataBindingUtil.inflate<ItemSongsBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_songs, parent, false))
            else -> ViewHolder(DataBindingUtil.inflate<ItemSongsBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_songs, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            typeSongHeader -> (holder as HeaderViewHolder).bind(songs!!.size)
            typeSongItem -> (holder as ViewHolder).bind(songs!![position])

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (showHeader && position == 0) typeSongHeader else typeSongItem
    }

    override fun getItemCount(): Int {
        return songs?.let {
            //extra total song count and sorting header
            if (showHeader)
                it.size + 1
            else it.size
        } ?: 0
    }

    class HeaderViewHolder constructor(var binding: ItemSongsHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(count: Int) {
            binding.songCount = count
            binding.executePendingBindings()
        }
    }

    class ViewHolder constructor(var binding: ItemSongsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.song = song
            binding.executePendingBindings()
        }
    }

    fun updateData(songs: List<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }
}