/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemAlbumBinding
import com.naman14.timberx.databinding.ItemArtistAlbumBinding
import com.naman14.timberx.models.Album
import com.naman14.timberx.util.extensions.dpToPixels
import com.naman14.timberx.util.extensions.inflateWithBinding

class AlbumAdapter constructor(private val isArtistAlbum: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var albums: List<Album> = emptyList()
        private set

    fun updateData(albums: List<Album>) {
        this.albums = albums
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (isArtistAlbum) {
            ArtistAlbumViewHolder(parent.inflateWithBinding(R.layout.item_artist_album))
        } else {
            ViewHolder(parent.inflateWithBinding(R.layout.item_album))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isArtistAlbum) {
            val albumHolder = holder as ArtistAlbumViewHolder
            albumHolder.run {
                artistAlbumBinding.albumTitle.setSingleLine()
                (artistAlbumBinding.rootView.layoutParams as RecyclerView.LayoutParams).rightMargin =
                        24f.dpToPixels(artistAlbumBinding.root.context)

                bind(albums[position])
            }
        } else {
            (holder as ViewHolder).bind(albums[position])
        }
    }

    override fun getItemCount() = albums.size

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
}
