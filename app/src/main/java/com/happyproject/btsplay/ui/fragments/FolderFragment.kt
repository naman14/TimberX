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
package com.happyproject.btsplay.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.rxkprefs.Pref
import com.happyproject.btsplay.PREF_LAST_FOLDER
import com.happyproject.btsplay.R
import com.happyproject.btsplay.databinding.LayoutRecyclerviewPaddingBinding
import com.happyproject.btsplay.extensions.getExtraBundle
import com.happyproject.btsplay.extensions.inflateWithBinding
import com.happyproject.btsplay.extensions.safeActivity
import com.happyproject.btsplay.repository.FoldersRepository
import com.happyproject.btsplay.repository.SongsRepository
import com.happyproject.btsplay.ui.adapters.FolderAdapter
import com.happyproject.btsplay.ui.fragments.base.MediaItemFragment
import com.happyproject.btsplay.util.AutoClearedValue
import org.koin.android.ext.android.inject

class FolderFragment : MediaItemFragment() {
    private lateinit var folderAdapter: FolderAdapter

    private val songsRepository by inject<SongsRepository>()
    private val foldersRepository by inject<FoldersRepository>()
    private val lastFolderPref by inject<Pref<String>>(name = PREF_LAST_FOLDER)

    var binding by AutoClearedValue<LayoutRecyclerviewPaddingBinding>(this)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.layout_recyclerview_padding, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        folderAdapter = FolderAdapter(safeActivity, songsRepository, foldersRepository, lastFolderPref)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = folderAdapter
        }
        folderAdapter.init(callback = { song, queueIds, title ->
            val extras = getExtraBundle(queueIds, title)
            mainViewModel.mediaItemClicked(song, extras)
        })
    }
}
