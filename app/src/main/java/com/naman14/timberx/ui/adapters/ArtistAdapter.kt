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
import com.naman14.timberx.databinding.ItemArtistBinding
import com.naman14.timberx.models.Artist
import com.naman14.timberx.extensions.inflateWithBinding

class ArtistAdapter : RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {
    var artists: List<Artist> = emptyList()
        private set

    init {
        setHasStableIds(true)
    }

    fun updateData(artists: List<Artist>) {
        this.artists = artists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateWithBinding(R.layout.item_artist))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.albumArt.setImageDrawable(null)
        holder.bind(artists[position])
    }

    override fun getItemCount() = artists.size

    override fun getItemId(position: Int) = artists[position].id

    class ViewHolder constructor(var binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.artist = artist
            binding.executePendingBindings()
        }
    }
}
