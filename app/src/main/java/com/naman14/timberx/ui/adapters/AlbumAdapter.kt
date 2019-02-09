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
import com.naman14.timberx.databinding.ItemAlbumsHeaderBinding
import com.naman14.timberx.databinding.ItemArtistAlbumBinding
import com.naman14.timberx.models.Album
import com.naman14.timberx.extensions.dpToPixels
import com.naman14.timberx.extensions.inflateWithBinding
import com.naman14.timberx.ui.listeners.SortMenuListener

private const val TYPE_ALBUM_HEADER = 0
private const val TYPE_ALBUM_ITEM = 1

class AlbumAdapter constructor(private val isArtistAlbum: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var showHeader = false
    var sortMenuListener: SortMenuListener? = null

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
            return when (viewType) {
                TYPE_ALBUM_HEADER -> {
                    val viewBinding = parent.inflateWithBinding<ItemAlbumsHeaderBinding>(R.layout.item_albums_header)
                    HeaderViewHolder(viewBinding, sortMenuListener)
                }
                TYPE_ALBUM_ITEM -> {
                    val viewBinding = parent.inflateWithBinding<ItemAlbumBinding>(R.layout.item_album)
                    ViewHolder(viewBinding)
                }
                else -> {
                    val viewBinding = parent.inflateWithBinding<ItemAlbumBinding>(R.layout.item_album)
                    ViewHolder(viewBinding)
                }
            }
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
            when (getItemViewType(position)) {
                TYPE_ALBUM_HEADER -> {
                    (holder as HeaderViewHolder).bind(albums.size)
                }
                TYPE_ALBUM_ITEM -> {
                    val album = albums[position + if (showHeader) -1 else 0]
                    (holder as ViewHolder).bind(album)
                }
            }
        }
    }

    override fun getItemCount() = if (showHeader) {
        albums.size + 1
    } else {
        albums.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (showHeader && position == 0) {
            TYPE_ALBUM_HEADER
        } else {
            TYPE_ALBUM_ITEM
        }
    }

    class HeaderViewHolder constructor(var binding: ItemAlbumsHeaderBinding, private val sortMenuListener: SortMenuListener?) : RecyclerView.ViewHolder(binding.root) {

        fun bind(count: Int) {
            binding.albumCount = count
            binding.executePendingBindings()

            binding.btnShuffle.setOnClickListener { sortMenuListener?.shuffleAll() }
            binding.sortMenu.setupMenu(sortMenuListener)
        }
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

    fun getAlbumForPosition(position: Int): Album? {
        return if (showHeader) {
            if (position == 0) {
                null
            } else {
                albums[position - 1]
            }
        } else {
            albums[position]
        }
    }
}
