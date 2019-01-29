package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.util.doAsyncPostWithResult
import kotlinx.android.synthetic.main.fragment_queue.*

class QueueFragment : BaseNowPlayingFragment() {

    companion object {
        fun newInstance() = QueueFragment()
    }

    lateinit var adapter: SongsAdapter

    private var initialItemsFetched = false
    private var allItemsFetched = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_queue, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = SongsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        nowPlayingViewModel.queueData.observe(this, Observer {
            tvQueueTitle.text = it?.queueTitle

            Handler().postDelayed({
                if (it.queue.isNotEmpty()) {
                    fetchQueueSongs(it.queue)
                }
            }, 200)

        })
    }

    private fun fetchQueueSongs(queue: LongArray) {
        doAsyncPostWithResult(handler = {
            if (queue.size > 10 && !initialItemsFetched) {
                initialItemsFetched = true
                SongsRepository.getSongsForIDs(activity!!, queue.asList().take(10).toLongArray())
            } else {
                allItemsFetched = true
                SongsRepository.getSongsForIDs(activity!!, queue)
            }
        }, postHandler = {
            if (it != null)
                adapter.updateData(it)
            if (initialItemsFetched && !allItemsFetched) fetchQueueSongs(queue)
        }).execute()
    }
}