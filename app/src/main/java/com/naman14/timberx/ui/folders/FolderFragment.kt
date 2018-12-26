package com.naman14.timberx.ui.folders

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.MediaItemFragment

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentFoldersBinding
import kotlinx.android.synthetic.main.fragment_songs.*
import com.naman14.timberx.util.*

class FolderFragment : MediaItemFragment() {

    lateinit var viewModel: FolderViewModel

    var binding by AutoClearedValue<FragmentFoldersBinding>(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_folders, container, false)

        return  binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val adapter = FolderAdapter(activity!!)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        adapter.init()

        viewModel = ViewModelProviders.of(this).get(FolderViewModel::class.java)

//        mediaItemFragmentViewModel.mediaItems.observe(this,
//                Observer<List<MediaBrowserCompat.MediaItem>> { list ->
//                    val isEmptyList = list?.isEmpty() ?: true
//                    if (!isEmptyList) {
//                        adapter.updateData(list as ArrayList<Playlist>)
//                    }
//                })

//        recyclerView.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
//            override fun onItemClick(position: Int, view: View) {
//                mainViewModel.mediaItemClicked(adapter.songs!![position], getExtraBundle(adapter.songs!!.toSongIDs(), "All songs"))
//            }
//        })
    }

}
