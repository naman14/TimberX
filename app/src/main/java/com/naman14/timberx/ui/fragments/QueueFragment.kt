package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentQueueBinding
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.ui.viewmodels.QueueViewModel
import com.naman14.timberx.util.AutoClearedValue
import com.naman14.timberx.util.doAsyncPostWithResult
import kotlinx.android.synthetic.main.fragment_queue.*

class QueueFragment: BaseNowPlayingFragment() {

    companion object {
        fun newInstance() = QueueFragment()
    }

    lateinit var viewModel: QueueViewModel

    var binding by AutoClearedValue<FragmentQueueBinding>(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_queue, container, false)

        return  binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = SongsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(QueueViewModel::class.java)

        nowPlayingViewModel.queueData.observe(this, Observer {
            tvQueueTitle.text = it?.queueTitle

            if (it.queue != null) {
                doAsyncPostWithResult(handler = {
                    SongsRepository.getSongsForIDs(activity!!, it.queue!!)
                }, postHandler = {
                    if (it != null)
                        adapter.updateData(it)
                }).execute()
            }
        })

    }
}