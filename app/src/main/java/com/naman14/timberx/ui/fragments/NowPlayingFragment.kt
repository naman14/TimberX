package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer

import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentNowPlayingBinding
import com.naman14.timberx.models.QueueData
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.*
import com.naman14.timberx.ui.activities.MainActivity
import kotlinx.android.synthetic.main.fragment_now_playing.*

class NowPlayingFragment : BaseNowPlayingFragment() {

    var binding by AutoClearedValue<FragmentNowPlayingBinding>(this)

    private var queueData: QueueData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_now_playing, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.let {
            it.viewModel = nowPlayingViewModel
            it.setLifecycleOwner(this)

            nowPlayingViewModel.currentData.observe(this, Observer {
                setNextData()
            })

            nowPlayingViewModel.queueData.observe(this, Observer { queueData ->
               this.queueData = queueData
                setNextData()
            })
        }

        setupUI()
    }

    private fun setNextData() {
        if (queueData == null) return

        val queue = queueData!!.queue
        if (queue != null && queue.isNotEmpty() && nowPlayingViewModel.currentData.value != null && activity != null) {

            val currentIndex = queue.indexOf(queueData!!.currentId!!)
            if (currentIndex + 1 < queue.size) {
                val nextSong = SongsRepository.getSongForId(activity!!, queue[currentIndex + 1])

                setImageUrl(upNextAlbumArt, nextSong.albumId)
                upNextTitle.text = nextSong.title
                upNextArtist.text = nextSong.artist
            } else {
                //nothing up next, show same
                upNextAlbumArt.setImageResource(R.drawable.ic_music_note)
                upNextTitle.text =  "Queue ended"
                upNextArtist.text = "No song up next"
            }


        }
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

        btnQueue.setOnClickListener {
            (activity as MainActivity).addFragment(QueueFragment())
        }

        btnBack.setOnClickListener {
            activity!!.onBackPressed()
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
}
