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
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.databinding.ItemArtistBinding
import com.naman14.timberx.models.Artist

class ArtistAdapter: RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {

    var artists: ArrayList<Artist>? = null

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(DataBindingUtil.inflate<ItemArtistBinding>(LayoutInflater.from(parent.context),
                R.layout.item_artist, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.albumArt.setImageDrawable(null)
        holder.bind(artists!![position])
    }

    override fun getItemCount(): Int {
        return artists?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return artists?.let { artists!![position].id } ?: super.getItemId(position)
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