package com.naman14.timberx.ui.songs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemSongsBinding
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.vo.Song

class SongsAdapter: RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    var songs: ArrayList<Song>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemSongsBinding>(LayoutInflater.from(parent.context),
                R.layout.item_songs, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(songs!![position])
    }

    override fun getItemCount(): Int {
        return songs?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemSongsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.song = song
            binding.executePendingBindings()
        }
    }

    fun updateData(songs: ArrayList<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }
}