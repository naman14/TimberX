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
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.constants.Constants.ACTION_QUEUE_REORDER
import com.naman14.timberx.constants.Constants.QUEUE_FROM
import com.naman14.timberx.constants.Constants.QUEUE_TO
import com.naman14.timberx.extensions.addOnItemClick
import com.naman14.timberx.extensions.getExtraBundle
import com.naman14.timberx.extensions.inflateTo
import com.naman14.timberx.extensions.keepInOrder
import com.naman14.timberx.extensions.observe
import com.naman14.timberx.extensions.toSongIds
import com.naman14.timberx.models.QueueData
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.ui.fragments.base.BaseNowPlayingFragment
import com.naman14.timberx.ui.widgets.DragSortRecycler
import kotlinx.android.synthetic.main.fragment_queue.recyclerView
import kotlinx.android.synthetic.main.fragment_queue.tvQueueTitle
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class QueueFragment : BaseNowPlayingFragment() {
    lateinit var adapter: SongsAdapter
    private lateinit var queueData: QueueData
    private var isReorderFromUser = false

    private val songsRepository by inject<SongsRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflateTo(R.layout.fragment_queue, container)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = SongsAdapter().apply {
            isQueue = true
            popupMenuListener = mainViewModel.popupMenuListener
        }
        recyclerView.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@QueueFragment.adapter
        }

        nowPlayingViewModel.queueData.observe(this) { data ->
            this.queueData = data
            tvQueueTitle.text = data.queueTitle
            if (data.queue.isNotEmpty()) {
                fetchQueueSongs(data.queue)
            }
        }

        recyclerView.addOnItemClick { position, _ ->
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

            recyclerView.run {
                addItemDecoration(dragSortRecycler)
                addOnItemTouchListener(dragSortRecycler)
                addOnScrollListener(dragSortRecycler.scrollListener)
            }
        }
    }
}
