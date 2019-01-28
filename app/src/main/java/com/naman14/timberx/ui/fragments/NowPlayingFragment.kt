package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentNowPlayingBinding
import com.naman14.timberx.util.*
import com.naman14.timberx.ui.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_now_playing.*

class NowPlayingFragment : BaseNowPlayingFragment() {

    var binding by AutoClearedValue<FragmentNowPlayingBinding>(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_now_playing, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.let {
            nowPlayingViewModel.currentData.observe(this, Observer {

            })

            it.viewModel = nowPlayingViewModel
            it.setLifecycleOwner(this)

            (activity as MainActivity).hideBottomSheet()
        }

        setupUI()
    }

    private fun setupUI() {

        songTitle.isSelected = true

        btnTogglePlayPause.setOnClickListener {
            nowPlayingViewModel.currentData.value?.let { mediaData ->
                mainViewModel.mediaItemClicked(mediaData.toDummySong(), null)
            }
        }

        btnNext.setOnClickListener {
            mainViewModel.transportControls().skipToNext()
        }

        btnPrevious.setOnClickListener {
            mainViewModel.transportControls().skipToPrevious()
        }

        btnRepeat.setOnClickListener {
            when (nowPlayingViewModel.currentData.value?.repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_NONE ->
                    mainViewModel.transportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)
                PlaybackStateCompat.REPEAT_MODE_ONE ->
                    mainViewModel.transportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
                PlaybackStateCompat.REPEAT_MODE_ALL ->
                    mainViewModel.transportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
            }
        }

        btnShuffle.setOnClickListener {
            when (nowPlayingViewModel.currentData.value?.shuffleMode) {
                PlaybackStateCompat.SHUFFLE_MODE_NONE ->
                    mainViewModel.transportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
                PlaybackStateCompat.SHUFFLE_MODE_ALL ->
                    mainViewModel.transportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
            }
        }

        buildUIControls()

    }

    private fun buildUIControls() {
        mainViewModel.mediaController.observe(this, Observer { mediaController ->
            mediaController?.let {
                progressText.setMediaController(it)
                seekBar.setMediaController(it)
            }
        })


        btnLyrics.setOnClickListener {
            val currentSong = nowPlayingViewModel.currentData.value
            if (currentSong != null && currentSong.artist != null && currentSong.title != null) {
                (activity as MainActivity).addFragment(LyricsFragment.newInstance(currentSong.artist!!, currentSong.title!!))
            }

        }

    }

    override fun onStop() {
        progressText.disconnectController()
        seekBar.disconnectController()
        super.onStop()
    }

    override fun onResume() {
        (activity as MainActivity).hideBottomSheet()
        super.onResume()
    }

    override fun onPause() {
        (activity as MainActivity).showBottomSheet()
        super.onPause()
    }

}
