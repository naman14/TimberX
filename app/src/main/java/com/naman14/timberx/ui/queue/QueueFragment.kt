package com.naman14.timberx.ui.queue

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentQueueBinding
import com.naman14.timberx.ui.songs.SongsAdapter
import com.naman14.timberx.util.AutoClearedValue
import com.naman14.timberx.util.toSongList
import kotlinx.android.synthetic.main.fragment_songs.*

class QueueFragment: Fragment() {

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

        viewModel.getQueueSongs().observe(this, Observer{ songs ->

            adapter.updateData(ArrayList(songs!!.toSongList()))
        })

        viewModel.getQueueData().observe(this, Observer {
            if (it != null)
            Log.e("lol", it.currentId.toString())
        })

    }
}