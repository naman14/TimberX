/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentLyricsBinding
import com.naman14.timberx.network.Outcome
import com.naman14.timberx.network.api.LyricsDataHandler
import com.naman14.timberx.ui.fragments.base.BaseNowPlayingFragment
import com.naman14.timberx.util.AutoClearedValue
import com.naman14.timberx.constants.Constants.ARTIST
import com.naman14.timberx.constants.Constants.SONG
import com.naman14.timberx.extensions.argument
import com.naman14.timberx.extensions.inflateWithBinding

class LyricsFragment : BaseNowPlayingFragment() {
    companion object {
        fun newInstance(artist: String, title: String): LyricsFragment {
            return LyricsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARTIST, artist)
                    putString(SONG, title)
                }
            }
        }
    }

    private lateinit var artistName: String
    lateinit var songTitle: String
    var binding by AutoClearedValue<FragmentLyricsBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_lyrics, container)
        artistName = argument(ARTIST)
        songTitle = argument(SONG)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.songTitle = songTitle

        // TODO make the lyrics handler/repo injectable
        LyricsDataHandler.lyricsRepository
                .getLyrics(artistName, songTitle)
                .observe(this, Observer {
                    when (it) {
                        is Outcome.Success -> binding.lyrics = it.data
                    }
                })
    }
}
