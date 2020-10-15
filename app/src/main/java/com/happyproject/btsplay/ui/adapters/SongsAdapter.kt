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
package com.happyproject.btsplay.ui.adapters

import android.support.v4.media.session.PlaybackStateCompat
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.happyproject.btsplay.R
import com.happyproject.btsplay.databinding.ItemSongsBinding
import com.happyproject.btsplay.databinding.ItemSongsHeaderBinding
import com.happyproject.btsplay.extensions.*
import com.happyproject.btsplay.models.Song
import com.happyproject.btsplay.ui.listeners.PopupMenuListener
import com.happyproject.btsplay.ui.listeners.SortMenuListener
import com.happyproject.btsplay.ui.viewmodels.NowPlayingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val PLAYLIST_ID_NOT_IN_PLAYLIST = -1L
private const val TYPE_SONG_HEADER = 0
private const val TYPE_SONG_ITEM = 1
private const val INVALID_POSITION = -1

class SongsAdapter(val lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var songs: List<Song> = emptyList()
    var showHeader = false
    var isQueue = false

    var popupMenuListener: PopupMenuListener? = null
    var sortMenuListener: SortMenuListener? = null

    var playlistId: Long = PLAYLIST_ID_NOT_IN_PLAYLIST

    private val nowPlayingViewModel by lifecycleOwner.viewModel<NowPlayingViewModel>()
    private var nowPlayingPosition = INVALID_POSITION

    init {
        // attach observer for updating now playing indicator on songs
        nowPlayingViewModel.currentData.observe(lifecycleOwner) {
            val previousPlayingPosition = nowPlayingPosition

            if (!it.mediaId.isNullOrEmpty()
                    && it.state == PlaybackStateCompat.STATE_PLAYING) {
                nowPlayingPosition = getPositionForSong(it.mediaId!!.toLong())
            } else {
                nowPlayingPosition = INVALID_POSITION
            }

            // reset playing indicator on previous playing position
            if (previousPlayingPosition != INVALID_POSITION)
                notifyItemChanged(previousPlayingPosition)

            // show playing indicator on now playing position
            if (nowPlayingPosition != INVALID_POSITION)
                notifyItemChanged(nowPlayingPosition)
        }
    }
    
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
                (holder as ViewHolder).bind(song, nowPlayingPosition)
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

        fun bind(song: Song, nowPlayingPosition: Int) {
            binding.song = song
            binding.albumArt.clipToOutline = true
            binding.executePendingBindings()

            binding.popupMenu.run {
                playlistId = this@ViewHolder.playlistId
                setupMenu(popupMenuListener) { song }
            }

            if (adapterPosition == nowPlayingPosition) {
                binding.visualizer.show()
            } else binding.visualizer.hide()

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

    private fun getPositionForSong(songId: Long): Int {
        val songIndex = songs.indexOf(songs.find { it.id == songId })
        return if (showHeader) {
             songIndex + 1
        } else songIndex
    }
}
