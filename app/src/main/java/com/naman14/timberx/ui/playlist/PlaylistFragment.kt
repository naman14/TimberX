package com.naman14.timberx.ui.playlist

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.MediaItemFragment

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentPlaylistsBinding
import com.naman14.timberx.databinding.FragmentSongsBinding;
import kotlinx.android.synthetic.main.fragment_songs.*
import com.naman14.timberx.util.*
import com.naman14.timberx.vo.Playlist

class PlaylistFragment : MediaItemFragment() {

    lateinit var viewModel: PlaylistViewModel

    var binding by AutoClearedValue<FragmentPlaylistsBinding>(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_playlists, container, false)

        return  binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = PlaylistAdapter()

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        viewModel = ViewModelProviders.of(this).get(PlaylistViewModel::class.java)

        mediaItemFragmentViewModel.mediaItems.observe(this,
                Observer<List<MediaBrowserCompat.MediaItem>> { list ->
                    val isEmptyList = list?.isEmpty() ?: true
                    if (!isEmptyList) {
                        adapter.updateData(list as ArrayList<Playlist>)
                    }
                })

//        recyclerView.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
//            override fun onItemClick(position: Int, view: View) {
//                mainViewModel.mediaItemClicked(adapter.songs!![position], getExtraBundle(adapter.songs!!.toSongIDs(), "All songs"))
//            }
//        })
    }

}
