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
import com.naman14.timberx.databinding.ItemSongsBinding
import com.naman14.timberx.databinding.ItemSongsHeaderBinding
import com.naman14.timberx.extensions.inflateWithBinding
import com.naman14.timberx.extensions.moveElement
import com.naman14.timberx.extensions.showOrHide
import com.naman14.timberx.models.Song
import com.naman14.timberx.ui.listeners.PopupMenuListener
import com.naman14.timberx.ui.listeners.SortMenuListener

private const val PLAYLIST_ID_NOT_IN_PLAYLIST = -1L
private const val TYPE_SONG_HEADER = 0
private const val TYPE_SONG_ITEM = 1

class SongsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var songs: List<Song> = emptyList()
    var showHeader = false
    var isQueue = false

    var popupMenuListener: PopupMenuListener? = null
    var sortMenuListener: SortMenuListener? = null

    var playlistId: Long = PLAYLIST_ID_NOT_IN_PLAYLIST

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SONG_HEADER -> {
                val viewBinding = parent.inflateWithBinding<ItemSongsHeaderBinding>(R.layout.item_songs_header)
                HeaderViewHolder(viewBinding, sortMenuListener)
            }
            TYPE_SONG_ITEM -> {
                val viewBinding = parent.inflateWithBinding<ItemSongsBinding>(R.layout.item_songs)
                ViewHolder(viewBinding, popupMenuListener, playlistId, isQueue)
            }
            else -> {
                val viewBinding = parent.inflateWithBinding<ItemSongsBinding>(R.layout.item_songs)
                ViewHolder(viewBinding, popupMenuListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_SONG_HEADER -> {
                (holder as HeaderViewHolder).bind(songs.size)
            }
            TYPE_SONG_ITEM -> {
                val song = songs[position + if (showHeader) -1 else 0]
                (holder as ViewHolder).bind(song)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (showHeader && position == 0) {
            TYPE_SONG_HEADER
        } else {
            TYPE_SONG_ITEM
        }
    }

    override fun getItemCount() = if (showHeader) {
        songs.size + 1
    } else {
        songs.size
    }

    class HeaderViewHolder constructor(var binding: ItemSongsHeaderBinding, private val sortMenuListener: SortMenuListener?) : RecyclerView.ViewHolder(binding.root) {

        fun bind(count: Int) {
            binding.songCount = count
            binding.executePendingBindings()

            binding.btnShuffle.setOnClickListener { sortMenuListener?.shuffleAll() }
            binding.sortMenu.setupMenu(sortMenuListener)
        }
    }

    class ViewHolder constructor(
        private val binding: ItemSongsBinding,
        private val popupMenuListener: PopupMenuListener?,
        private val playlistId: Long = -1,
        private val isQueue: Boolean = false
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.song = song
            binding.albumArt.clipToOutline = true
            binding.executePendingBindings()

            binding.popupMenu.run {
                playlistId = this@ViewHolder.playlistId
                setupMenu(popupMenuListener) { song }
            }

            binding.ivReorder.showOrHide(isQueue)
        }
    }

    fun updateData(songs: List<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun reorderSong(from: Int, to: Int) {
        songs.moveElement(from, to)
        notifyItemMoved(from, to)
    }

    fun getSongForPosition(position: Int): Song? {
        return if (showHeader) {
            if (position == 0) {
                null
            } else {
                songs[position - 1]
            }
        } else {
            songs[position]
        }
    }
}
