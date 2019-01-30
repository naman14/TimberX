package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.models.QueueData
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.ui.widgets.DragSortRecycler
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.util.*
import com.naman14.timberx.util.media.getExtraBundle
import kotlinx.android.synthetic.main.fragment_queue.*

class QueueFragment : BaseNowPlayingFragment() {

    companion object {
        fun newInstance() = QueueFragment()
    }

    lateinit var adapter: SongsAdapter

    private lateinit var queueData: QueueData

    private var isReorderFromUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_queue, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        adapter = SongsAdapter().apply {
            isQueue = true
            popupMenuListener = mainViewModel.popupMenuListener
        }

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        nowPlayingViewModel.queueData.observe(this, Observer {
            this.queueData = it
            tvQueueTitle.text = it?.queueTitle
            if (it.queue.isNotEmpty()) {
                fetchQueueSongs(it.queue)
            }
        })

        recyclerView.addOnItemClick(object : RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                adapter.getSongForPosition(position)?.let { song ->
                    mainViewModel.mediaItemClicked(song,
                            getExtraBundle(adapter.songs!!.toSongIDs(), queueData.queueTitle))
                }
            }
        })
    }

    private fun fetchQueueSongs(queue: LongArray) {
        //to avoid lag when reordering queue, we dont refetch queue if we know the reorder was from user
        if (isReorderFromUser) {
            isReorderFromUser = false
            return
        }

        doAsyncPostWithResult(handler = {
            SongsRepository.getSongsForIDs(activity!!, queue).keepInOrder(queue)
        }, postHandler = {
            if (it != null) {
                adapter.updateData(it)

                val dragSortRecycler = DragSortRecycler()
                dragSortRecycler.setViewHandleId(R.id.ivReorder)

                dragSortRecycler.setOnItemMovedListener { from, to ->
                    isReorderFromUser = true
                    adapter.reorderSong(from, to)
                    mainViewModel.transportControls().sendCustomAction(Constants.ACTION_QUEUE_REORDER, Bundle().apply {
                        putInt(Constants.QUEUE_FROM, from)
                        putInt(Constants.QUEUE_TO, to)
                    })
                }

                recyclerView.addItemDecoration(dragSortRecycler)
                recyclerView.addOnItemTouchListener(dragSortRecycler)
                recyclerView.addOnScrollListener(dragSortRecycler.scrollListener)
            }
        }).execute()
    }
}