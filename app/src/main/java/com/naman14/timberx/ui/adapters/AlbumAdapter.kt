package com.naman14.timberx.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemAlbumBinding
import com.naman14.timberx.databinding.ItemArtistAlbumBinding
import com.naman14.timberx.models.Album
import com.naman14.timberx.util.Utils

class AlbumAdapter constructor(private val isArtistAlbum: Boolean = false): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var albums: ArrayList<Album>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (isArtistAlbum) {
            return ArtistAlbumViewHolder(DataBindingUtil.inflate<ItemArtistAlbumBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_artist_album, parent, false))
        } else {
            return ViewHolder(DataBindingUtil.inflate<ItemAlbumBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_album, parent, false))
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isArtistAlbum) {
            (holder as ArtistAlbumViewHolder).apply {
                artistAlbumBinding.albumTitle.setSingleLine()
                (artistAlbumBinding.rootView.layoutParams as RecyclerView.LayoutParams).rightMargin =
                        Utils.convertDpToPixel(24f, artistAlbumBinding.root.context).toInt()

                bind(albums!![position])
            }

        } else {
            (holder as ViewHolder).bind(albums!![position])
        }
    }

    override fun getItemCount(): Int {
        return albums?.size ?: 0
    }

    class ViewHolder constructor(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {

            binding.albumArt.clipToOutline = true
            binding.album = album
            binding.executePendingBindings()

        }
    }

    class ArtistAlbumViewHolder constructor(val artistAlbumBinding: ItemArtistAlbumBinding) : RecyclerView.ViewHolder(artistAlbumBinding.root) {

        fun bind(album: Album) {
            artistAlbumBinding.albumArt.clipToOutline = true
            artistAlbumBinding.album = album
            artistAlbumBinding.executePendingBindings()

        }
    }

    fun updateData(albums: ArrayList<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }
}