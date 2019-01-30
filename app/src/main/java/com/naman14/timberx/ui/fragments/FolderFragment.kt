package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager

import com.naman14.timberx.R
import com.naman14.timberx.ui.adapters.FolderAdapter
import com.naman14.timberx.util.media.getExtraBundle
import kotlinx.android.synthetic.main.layout_recyclerview_padding.*

class FolderFragment : MediaItemFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recyclerview_padding, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = FolderAdapter(activity!!)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        adapter.init(callback = { song, queueIds, title ->
            mainViewModel.mediaItemClicked(song, getExtraBundle(queueIds, title))
        })

    }

}
