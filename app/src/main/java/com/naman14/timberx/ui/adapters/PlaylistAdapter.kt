package com.naman14.timberx.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemPlaylistBinding
import com.naman14.timberx.vo.Playlist

class PlaylistAdapter: RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    var playlists: ArrayList<Playlist>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemPlaylistBinding>(LayoutInflater.from(parent.context),
                R.layout.item_playlist, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playlists!![position])
    }

    override fun getItemCount(): Int {
        return playlists?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlist = playlist
            binding.executePendingBindings()
        }
    }

    fun updateData(playlists: ArrayList<Playlist>) {
        this.playlists = playlists
        notifyDataSetChanged()
    }
}