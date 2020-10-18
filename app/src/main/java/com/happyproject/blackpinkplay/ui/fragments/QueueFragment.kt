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
package com.happyproject.blackpinkplay.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.constants.Constants.ACTION_QUEUE_REORDER
import com.happyproject.blackpinkplay.constants.Constants.QUEUE_FROM
import com.happyproject.blackpinkplay.constants.Constants.QUEUE_TO
import com.happyproject.blackpinkplay.databinding.FragmentQueueBinding
import com.happyproject.blackpinkplay.extensions.*
import com.happyproject.blackpinkplay.models.QueueData
import com.happyproject.blackpinkplay.repository.SongsRepository
import com.happyproject.blackpinkplay.ui.adapters.SongsAdapter
import com.happyproject.blackpinkplay.ui.fragments.base.BaseNowPlayingFragment
import com.happyproject.blackpinkplay.ui.widgets.DragSortRecycler
import com.happyproject.blackpinkplay.util.AutoClearedValue
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class QueueFragment : BaseNowPlayingFragment() {
    lateinit var adapter: SongsAdapter
    private lateinit var queueData: QueueData
    private var isReorderFromUser = false

    private val songsRepository by inject<SongsRepository>()

    var binding by AutoClearedValue<FragmentQueueBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_queue, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = SongsAdapter(this).apply {
            isQueue = true
            popupMenuListener = mainViewModel.popupMenuListener
        }
        binding.recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@QueueFragment.adapter
        }

        nowPlayingViewModel.queueData.observe(this) { data ->
            this.queueData = data
            binding.tvQueueTitle.text = data.queueTitle
            if (data.queue.isNotEmpty()) {
                fetchQueueSongs(data.queue)
            }
        }

        binding.recyclerView.addOnItemClick { position, _ ->
            adapter.getSongForPosition(position)?.let { song ->
                val extras = getExtraBundle(adapter.songs.toSongIds(), queueData.queueTitle)
                mainViewModel.mediaItemClicked(song, extras)
            }
        }
    }

    private fun fetchQueueSongs(queue: LongArray) {
        //to avoid lag when reordering queue, we don't re-fetch queue if we know the reorder was from user
        if (isReorderFromUser) {
            isReorderFromUser = false
            return
        }

        // TODO should this logic be in a view model?
        launch {
            val songs = withContext(IO) {
                songsRepository.getSongsForIds(queue).keepInOrder(queue)
            } ?: return@launch
            adapter.updateData(songs)

            val dragSortRecycler = DragSortRecycler().apply {
                setViewHandleId(R.id.ivReorder)
                setOnItemMovedListener { from, to ->
                    isReorderFromUser = true
                    adapter.reorderSong(from, to)

                    val extras = Bundle().apply {
                        putInt(QUEUE_FROM, from)
                        putInt(QUEUE_TO, to)
                    }
                    mainViewModel.transportControls().sendCustomAction(ACTION_QUEUE_REORDER, extras)
                }
            }

            binding.recyclerView.run {
                addItemDecoration(dragSortRecycler)
                addOnItemTouchListener(dragSortRecycler)
                addOnScrollListener(dragSortRecycler.scrollListener)
            }
        }
    }
}
