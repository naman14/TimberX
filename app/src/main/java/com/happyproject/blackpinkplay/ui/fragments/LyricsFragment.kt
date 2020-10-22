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
package com.happyproject.blackpinkplay.ui.fragments

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.constants.Constants
import com.happyproject.blackpinkplay.constants.Constants.ALBUM
import com.happyproject.blackpinkplay.constants.Constants.ARTIST
import com.happyproject.blackpinkplay.constants.Constants.SONG
import com.happyproject.blackpinkplay.databinding.FragmentLyricsBinding
import com.happyproject.blackpinkplay.extensions.argument
import com.happyproject.blackpinkplay.extensions.disposeOnDetach
import com.happyproject.blackpinkplay.extensions.inflateWithBinding
import com.happyproject.blackpinkplay.extensions.ioToMain
import com.happyproject.blackpinkplay.extensions.subscribeForOutcome
import com.happyproject.blackpinkplay.network.Outcome
import com.happyproject.blackpinkplay.network.api.LyricsRestService
import com.happyproject.blackpinkplay.ui.fragments.base.BaseNowPlayingFragment
import com.happyproject.blackpinkplay.util.AutoClearedValue
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagException
import org.koin.android.ext.android.inject
import java.io.File
import java.io.IOException
import java.lang.Exception

class LyricsFragment : BaseNowPlayingFragment() {
    companion object {
        fun newInstance(artist: String, songTitle: String, albumTitle: String): LyricsFragment {
            return LyricsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARTIST, artist)
                    putString(SONG, songTitle)
                    putString(ALBUM, albumTitle)
                }
            }
        }
    }

    private lateinit var artistName: String
    lateinit var songTitle: String
    lateinit var albumTitle: String
    var binding by AutoClearedValue<FragmentLyricsBinding>(this)

    private val lyricsService by inject<LyricsRestService>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_lyrics, container)
        artistName = argument(ARTIST)
        songTitle = argument(SONG)
        albumTitle = argument(ALBUM)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.songTitle = songTitle

        val lyrics = getLyricsLocal(songTitle, albumTitle)
        if (lyrics.isNotEmpty()) {
            binding.lyrics = lyrics
        } else {
            // TODO make the lyrics handler/repo injectable

            lyricsService.getLyrics(artistName, songTitle)
                .ioToMain()
                .subscribeForOutcome { outcome ->
                    when (outcome) {
                        is Outcome.Success -> binding.lyrics = outcome.data
                    }
                }
                .disposeOnDetach(view)
        }
    }

    private fun getLyricsLocal(songTitle: String, albumTitle: String): String {
        val fileName = "$albumTitle-$songTitle.mp3".replace(" ","")
        val path = Environment.getExternalStorageDirectory().toString() + "/" + Constants.APP_PACKAGE_NAME + "/$fileName"
        var lyrics = ""
        val file = File(path)
        if (file.exists()) {
            try {
                val audioFile = AudioFileIO.read(file)
                if (audioFile != null) {
                    val tag = audioFile.tag
                    if (tag != null) {
                        val tagLyrics = tag.getFirst(FieldKey.LYRICS)
                        if (!tagLyrics.isNullOrEmpty()) {
                            lyrics = tagLyrics.replace("\r", "\n")
                        }
                    }
                }
            } catch (ignored: CannotReadException) {
            } catch (ignored: IOException) {
            } catch (ignored: TagException) {
            } catch (ignored: ReadOnlyFileException) {
            } catch (ignored: InvalidAudioFrameException) {
            } catch (ignored: UnsupportedOperationException) {
            } catch (ignored: Exception) {
            }
        }
        return lyrics
    }
}
