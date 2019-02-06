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
package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.extensions.addOnItemClick
import com.naman14.timberx.extensions.drawable
import com.naman14.timberx.extensions.filter
import com.naman14.timberx.extensions.inflateTo
import com.naman14.timberx.extensions.observe
import com.naman14.timberx.models.Genre
import com.naman14.timberx.ui.adapters.GenreAdapter
import com.naman14.timberx.ui.fragments.base.MediaItemFragment
import kotlinx.android.synthetic.main.layout_recyclerview_padding.recyclerView

class GenreFragment : MediaItemFragment() {

    private lateinit var genreAdapter: GenreAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.layout_recyclerview_padding, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        genreAdapter = GenreAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = genreAdapter
            addItemDecoration(DividerItemDecoration(activity, VERTICAL).apply {
                val divider = activity.drawable(R.drawable.divider)
                divider?.let { setDrawable(it) }
            })
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(genreAdapter.genres[position], null)
            }
        }

        mediaItemFragmentViewModel.mediaItems
                .filter { it.isNotEmpty() }
                .observe(this) { list ->
                    @Suppress("UNCHECKED_CAST")
                    genreAdapter.updateData(list as List<Genre>)
                }
    }
}
