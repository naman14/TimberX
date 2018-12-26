package com.naman14.timberx.ui.genre

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemAlbumBinding
import com.naman14.timberx.databinding.ItemGenreBinding
import com.naman14.timberx.databinding.ItemPlaylistBinding
import com.naman14.timberx.databinding.ItemSongsBinding
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.vo.Album
import com.naman14.timberx.vo.Genre
import com.naman14.timberx.vo.Playlist
import com.naman14.timberx.vo.Song

class GenreAdapter: RecyclerView.Adapter<GenreAdapter.ViewHolder>() {

    var genres: ArrayList<Genre>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemGenreBinding>(LayoutInflater.from(parent.context),
                R.layout.item_genre, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(genres!![position])
    }

    override fun getItemCount(): Int {
        return genres?.size ?: 0
    }

    class ViewHolder constructor(var binding: ItemGenreBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: Genre) {
            binding.genre = genre
            binding.executePendingBindings()
        }
    }

    fun updateData(genres: ArrayList<Genre>) {
        this.genres = genres
        notifyDataSetChanged()
    }
}