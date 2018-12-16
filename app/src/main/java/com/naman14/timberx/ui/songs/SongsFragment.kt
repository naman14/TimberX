package com.naman14.timberx.ui.songs

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.MediaItemFragment

import com.naman14.timberx.R
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.databinding.FragmentSongsBinding;
import com.naman14.timberx.db.DbHelper
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import kotlinx.android.synthetic.main.fragment_songs.*
import com.naman14.timberx.util.*
import com.naman14.timberx.vo.Song

class SongsFragment : MediaItemFragment() {

    lateinit var viewModel: SongsViewModel

    var binding by AutoClearedValue<FragmentSongsBinding>(this)

    companion object {
        fun newInstance(): MediaItemFragment =  SongsFragment().apply {
            arguments = Bundle().apply {
                putString(TimberMusicService.MEDIA_ID_ARG, mediaId)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_songs, container, false)

        return  binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = SongsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(SongsViewModel::class.java)

        mediaItemFragmentViewModel.mediaItems.observe(this,
                Observer<List<MediaBrowserCompat.MediaItem>> { list ->
                    val isEmptyList = list?.isEmpty() ?: true
                    if (!isEmptyList) {
                        adapter.updateData(list as ArrayList<Song>)
                    }
                })

        recyclerView.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                mainActivityViewModel.mediaItemClicked(adapter.songs!![position], getExtraBundle(adapter.songs!!.toSongIDs(), "All songs"))
            }
        })
    }

}
