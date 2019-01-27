package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.naman14.timberx.R
import com.naman14.timberx.ui.adapters.GenreAdapter
import com.naman14.timberx.models.Genre
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.util.addOnItemClick
import kotlinx.android.synthetic.main.layout_recyclerview_padding.*

class GenreFragment : MediaItemFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_recyclerview_padding, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = GenreAdapter()

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter


        mediaItemFragmentViewModel.mediaItems.observe(this,
                Observer<List<MediaBrowserCompat.MediaItem>> { list ->
                    val isEmptyList = list?.isEmpty() ?: true
                    if (!isEmptyList) {
                        adapter.updateData(list as ArrayList<Genre>)
                    }
                })

        recyclerView.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                mainViewModel.mediaItemClicked(adapter.genres!![position], null)
            }
        })
    }

}
