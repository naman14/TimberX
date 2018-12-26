package com.naman14.timberx.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemArtistBinding
import com.naman14.timberx.vo.Artist

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