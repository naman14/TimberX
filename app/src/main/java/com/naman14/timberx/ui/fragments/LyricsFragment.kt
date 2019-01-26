package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentArtistDetailBinding
import com.naman14.timberx.databinding.FragmentLyricsBinding
import com.naman14.timberx.ui.adapters.AlbumAdapter
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.util.*
import com.naman14.timberx.util.SpacesItemDecoration
import com.naman14.timberx.models.Album
import com.naman14.timberx.models.Artist
import com.naman14.timberx.models.Song
import com.naman14.timberx.network.Outcome
import com.naman14.timberx.network.api.LyricsDataHandler
import kotlinx.android.synthetic.main.layout_recyclerview_padding.*

class LyricsFragment : NowPlayingFragment() {

    lateinit var song: Song

    var binding by AutoClearedValue<FragmentLyricsBinding>(this)

    companion object {
        fun newInstance(song: Song): LyricsFragment {
            return LyricsFragment().apply { arguments = Bundle().apply { putParcelable(Constants.SONG, song) } }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_lyrics, container, false)

        song = arguments!![Constants.SONG] as Song

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        LyricsDataHandler.lyricsRepository.getLyrics(song.artist, song.title).observe(this, Observer {
            when (it) {
                is Outcome.Success -> binding.lyrics = it.data
            }
        })

    }

}
