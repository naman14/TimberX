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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemSongsBinding
import com.naman14.timberx.databinding.ItemSongsHeaderBinding
import com.naman14.timberx.models.Song
import com.naman14.timberx.ui.listeners.PopupMenuListener
import com.naman14.timberx.ui.listeners.SortMenuListener
import com.naman14.timberx.util.moveElement

class SongsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var songs: List<Song>? = null
    var showHeader = false
    var isQueue = false

    var popupMenuListener: PopupMenuListener? = null
    var sortMenuListener: SortMenuListener? = null

    //-1 is its a normal song, will be set to a playlist id if the song is in a playlist
    var playlistId: Long = -1

    private val typeSongHeader = 0
    private val typeSongItem = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            typeSongHeader -> HeaderViewHolder(DataBindingUtil.inflate<ItemSongsHeaderBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_songs_header, parent, false), sortMenuListener)
            typeSongItem -> ViewHolder(DataBindingUtil.inflate<ItemSongsBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_songs, parent, false), popupMenuListener, playlistId, isQueue)
            else -> ViewHolder(DataBindingUtil.inflate<ItemSongsBinding>(LayoutInflater.from(parent.context),
                    R.layout.item_songs, parent, false), popupMenuListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            typeSongHeader -> (holder as HeaderViewHolder).bind(songs!!.size)
            typeSongItem -> (holder as ViewHolder).bind(songs!![position + if (showHeader) -1 else 0])

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

    class HeaderViewHolder constructor(var binding: ItemSongsHeaderBinding, private val sortMenuListener: SortMenuListener?) : RecyclerView.ViewHolder(binding.root) {

        fun bind(count: Int) {
            binding.songCount = count
            binding.executePendingBindings()

            binding.btnShuffle.setOnClickListener { sortMenuListener?.shuffleAll() }
            binding.sortMenu.setupMenu(sortMenuListener)

        }
    }

    class ViewHolder constructor(private val binding: ItemSongsBinding, private val popupMenuListener: PopupMenuListener?,
                                 private val playlistId: Long = -1,
                                 private val isQueue: Boolean = false) : RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {
            binding.song = song
            binding.executePendingBindings()

            binding.popupMenu.apply { playlistId = this@ViewHolder.playlistId }.setupMenu(popupMenuListener) {
                song
            }

            if (isQueue) {
                binding.ivReorder.visibility = View.VISIBLE
            }

        }
    }

    fun updateData(songs: List<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun reorderSong(from: Int, to: Int) {
        songs?.moveElement(from, to)
        notifyItemMoved(from, to)
    }

    fun getSongForPosition(position: Int): Song? {
        return if (showHeader) {
            if (position == 0) null else songs!![position - 1]
        } else songs!![position]
    }
}