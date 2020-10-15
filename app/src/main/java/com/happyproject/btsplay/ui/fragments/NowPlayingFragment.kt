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
package com.happyproject.btsplay.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.happyproject.btsplay.R
import com.happyproject.btsplay.databinding.FragmentNowPlayingBinding
import com.happyproject.btsplay.extensions.addFragment
import com.happyproject.btsplay.extensions.inflateWithBinding
import com.happyproject.btsplay.extensions.observe
import com.happyproject.btsplay.extensions.safeActivity
import com.happyproject.btsplay.models.QueueData
import com.happyproject.btsplay.network.models.ArtworkSize
import com.happyproject.btsplay.repository.SongsRepository
import com.happyproject.btsplay.ui.bindings.setLastFmAlbumImage
import com.happyproject.btsplay.ui.dialogs.AboutDialog
import com.happyproject.btsplay.ui.fragments.base.BaseNowPlayingFragment
import com.happyproject.btsplay.util.AutoClearedValue
import org.koin.android.ext.android.inject

class NowPlayingFragment : BaseNowPlayingFragment() {
    var binding by AutoClearedValue<FragmentNowPlayingBinding>(this)
    private var queueData: QueueData? = null

    private val songsRepository by inject<SongsRepository>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_now_playing, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

        binding.let {
            it.viewModel = nowPlayingViewModel
            it.lifecycleOwner = this

            nowPlayingViewModel.currentData.observe(this) { setNextData() }
            nowPlayingViewModel.queueData.observe(this) { queueData ->
                this.queueData = queueData
                setNextData()
            }
        }
        setupUI()
    }

    //TODO this should not here, move it to BindingAdapter or create a separate queue view model
    private fun setNextData() {
        val queue = queueData?.queue ?: return
        if (queue.isNotEmpty() && nowPlayingViewModel.currentData.value != null) {

            val currentIndex = queue.indexOf(nowPlayingViewModel.currentData.value!!.mediaId!!.toLong())
            if (currentIndex + 1 < queue.size) {
                val nextSong = songsRepository.getSongForId(queue[currentIndex + 1])
                setLastFmAlbumImage(binding.upNextAlbumArt, nextSong.artist, nextSong.album, ArtworkSize.MEDIUM, nextSong.albumId)
                binding.upNextTitle.text = nextSong.title
                binding.upNextArtist.text = nextSong.artist
            } else {
                //nothing up next, show same
                binding.upNextAlbumArt.setImageResource(R.drawable.ic_music_note)
                binding.upNextTitle.text = getString(R.string.queue_ended)
                binding.upNextArtist.text = getString(R.string.no_song_next)
            }
        }
    }

    private fun setupUI() {
        binding.songTitle.isSelected = true
        binding.btnTogglePlayPause.setOnClickListener {
            nowPlayingViewModel.currentData.value?.let { mediaData ->
                mainViewModel.mediaItemClicked(mediaData.toDummySong(), null)
            }
        }
        binding.btnNext.setOnClickListener {
            mainViewModel.transportControls().skipToNext()
        }
        binding.btnPrevious.setOnClickListener {
            mainViewModel.transportControls().skipToPrevious()
        }

        binding.btnRepeat.setOnClickListener {
            when (nowPlayingViewModel.currentData.value?.repeatMode) {
                REPEAT_MODE_NONE -> mainViewModel.transportControls().setRepeatMode(REPEAT_MODE_ONE)
                REPEAT_MODE_ONE -> mainViewModel.transportControls().setRepeatMode(REPEAT_MODE_ALL)
                REPEAT_MODE_ALL -> mainViewModel.transportControls().setRepeatMode(REPEAT_MODE_NONE)
            }
        }
        binding.btnShuffle.setOnClickListener {
            when (nowPlayingViewModel.currentData.value?.shuffleMode) {
                SHUFFLE_MODE_NONE -> mainViewModel.transportControls().setShuffleMode(SHUFFLE_MODE_ALL)
                SHUFFLE_MODE_ALL -> mainViewModel.transportControls().setShuffleMode(SHUFFLE_MODE_NONE)
            }
        }

        binding.btnQueue.setOnClickListener { safeActivity.addFragment(fragment = QueueFragment()) }
        binding.btnBack.setOnClickListener { safeActivity.onBackPressed() }

        buildUIControls()
    }

    private fun buildUIControls() {
        binding.btnLyrics.setOnClickListener {
            val currentSong = nowPlayingViewModel.currentData.value
            val artist = currentSong?.artist
            val title = currentSong?.title
            if (artist != null && title != null) {
                safeActivity.addFragment(fragment = LyricsFragment.newInstance(artist, title))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_about -> AboutDialog.show(safeActivity)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.mediaController.observe(this) { mediaController ->
            binding.progressText.setMediaController(mediaController)
            binding.seekBar.setMediaController(mediaController)
        }
    }

    override fun onStop() {
        binding.progressText.disconnectController()
        binding.seekBar.disconnectController()
        super.onStop()
    }
}
