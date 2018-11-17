package com.naman14.timberx.ui.albumdetail

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentAlbumDetailBinding
import com.naman14.timberx.ui.songs.SongsAdapter
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.util.*
import com.naman14.timberx.vo.Album
import kotlinx.android.synthetic.main.fragment_songs.*


class AlbumDetailFragment : Fragment() {

    companion object {
        fun newInstance(album: Album) = AlbumDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(Constants.ALBUM, album)
            }
        }
    }

    lateinit var viewModel: AlbumDetailViewModel
    lateinit var album: Album

    var binding by AutoClearedValue<FragmentAlbumDetailBinding>(this)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_album_detail, container, false)

        album = arguments!![Constants.ALBUM] as Album

        return  binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(AlbumDetailViewModel::class.java)
        binding.album = album

        val adapter = SongsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        viewModel.getAlbumSongs(album.id).observe(this, Observer {
            adapter.updateData(it!!)
        })

        recyclerView.addOnItemClick(object: RecyclerItemClickListener.OnClickListener {
            override fun onItemClick(position: Int, view: View) {
                getMediaController(activity!!)?.transportControls?.playFromMediaId(adapter.songs!![position].id.toString(),
                        getExtraBundle(adapter.songs!!.toSongIDs(), album.title))
            }
        })
    }

}
