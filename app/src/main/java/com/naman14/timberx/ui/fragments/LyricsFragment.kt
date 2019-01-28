package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentLyricsBinding
import com.naman14.timberx.util.*
import com.naman14.timberx.network.Outcome
import com.naman14.timberx.network.api.LyricsDataHandler

class LyricsFragment : BaseNowPlayingFragment() {

    lateinit var artistName: String
    lateinit var songTitle: String

    var binding by AutoClearedValue<FragmentLyricsBinding>(this)

    companion object {
        fun newInstance(artist: String, title: String): LyricsFragment {
            return LyricsFragment().apply { arguments = Bundle().apply {
                putString(Constants.ARTIST, artist)
                putString(Constants.SONG, title)
            } }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_lyrics, container, false)

        artistName = arguments!![Constants.ARTIST] as String
        songTitle = arguments!![Constants.SONG] as String

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.songTitle = songTitle

        LyricsDataHandler.lyricsRepository.getLyrics(artistName, songTitle).observe(this, Observer {
            when (it) {
                is Outcome.Success -> binding.lyrics = it.data
            }
        })

    }

}
