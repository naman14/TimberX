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
import com.naman14.timberx.databinding.ItemGenreBinding
import com.naman14.timberx.models.Genre

class GenreAdapter : RecyclerView.Adapter<GenreAdapter.ViewHolder>() {

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
