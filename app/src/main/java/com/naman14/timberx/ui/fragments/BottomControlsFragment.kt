package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naman14.timberx.ui.activities.MainActivity
import com.naman14.timberx.R
import com.naman14.timberx.databinding.LayoutBottomsheetControlsBinding
import com.naman14.timberx.models.CastStatus
import com.naman14.timberx.ui.bindings.setImageUrl
import com.naman14.timberx.ui.bindings.setPlayState
import com.naman14.timberx.ui.widgets.BottomSheetListener
import com.naman14.timberx.util.*
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*

class BottomControlsFragment : BaseNowPlayingFragment(), BottomSheetListener {

    var binding by AutoClearedValue<LayoutBottomsheetControlsBinding>(this)
    private var isCasting = false

    companion object {
        fun newInstance() = BottomControlsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.layout_bottomsheet_controls, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.rootView.setOnClickListener {
            if (!isCasting)
                (activity as MainActivity).addFragment(NowPlayingFragment(), Constants.NOW_PLAYING)
        }

        binding.viewModel = nowPlayingViewModel
        binding.lifecycleOwner = this

        setupUI()
        setupCast()
    }
    private fun setupUI() {

        val layoutParams = progressBar.layoutParams as LinearLayout.LayoutParams
        progressBar.measure(0, 0)
        layoutParams.setMargins(0, -(progressBar.measuredHeight / 2), 0, 0)
        progressBar.layoutParams = layoutParams
        songTitle.isSelected = true

        btnTogglePlayPause.setOnClickListener {
            nowPlayingViewModel.currentData.value?.let { mediaData ->
                mainViewModel.mediaItemClicked(mediaData.toDummySong(), null)
            }
        }

        btnPlayPause.setOnClickListener {
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

        if (activity is MainActivity) {
            (activity as MainActivity).also { activity ->
                btnCollapse.setOnClickListener { activity.collapseBottomSheet() }
                activity.setBottomSheetListener(this)
            }
        }

        buildUIControls()

    }

    private fun buildUIControls() {
        mainViewModel.mediaController.observe(this, Observer { mediaController ->
            mediaController?.let {
                progressBar.setMediaController(it)
                progressText.setMediaController(it)
                seekBar.setMediaController(it)
            }
        })


        btnLyrics.setOnClickListener {
            val currentSong = nowPlayingViewModel.currentData.value
            if (currentSong != null && currentSong.artist != null && currentSong.title != null) {
                if (activity is MainActivity) {
                    (activity as MainActivity).also { activity ->
                        activity.collapseBottomSheet()
                        Handler().postDelayed({
                            activity.addFragment(LyricsFragment.newInstance(currentSong.artist!!, currentSong.title!!))
                        }, 200)
                    }
                }
            }

        }

    }


    private fun setupCast() {
        //display cast data directly if casting instead of databinding

        val castProgressObserver = Observer<Pair<Long, Long>> {
            binding.progressBar.progress = it.first.toInt()
            if (binding.progressBar.max != it.second.toInt())
                binding.progressBar.max = it.second.toInt()

            binding.seekBar.progress = it.first.toInt()
            if (binding.seekBar.max != it.second.toInt())
                binding.seekBar.max = it.second.toInt()
        }

        val castStatusObserver = Observer<CastStatus> {
            it ?: return@Observer
            if (it.isCasting) {
                isCasting = true

                mainViewModel.castProgressLiveData.observe(this, castProgressObserver)

                setImageUrl(binding.bottomContolsAlbumart, it.castAlbumId.toLong())

                binding.songArtist.text = "Casting to " + it.castDeviceName
                if (it.castSongId == -1) {
                    binding.songTitle.text = "Nothing playing"
                } else binding.songTitle.text = it.castSongTitle + " | " + it.castSongArtist

                if (it.state == CastStatus.STATUS_PLAYING) {
                    setPlayState(binding.btnTogglePlayPause, PlaybackStateCompat.STATE_PLAYING)
                    setPlayState(binding.btnPlayPause, PlaybackStateCompat.STATE_PLAYING)
                } else {
                    setPlayState(binding.btnTogglePlayPause, PlaybackStateCompat.STATE_PAUSED)
                    setPlayState(binding.btnPlayPause, PlaybackStateCompat.STATE_PAUSED)
                }
            } else {
                isCasting = false
                mainViewModel.castProgressLiveData.removeObserver(castProgressObserver)
            }
        }


        mainViewModel.customAction.observe(this, Observer {
            it?.peekContent()?.let { action ->
                when (action) {
                    Constants.ACTION_CAST_CONNECTED -> {
                        mainViewModel.castLiveData.observe(this, castStatusObserver)
                    }
                    Constants.ACTION_CAST_DISCONNECTED -> {
                        isCasting = false
                        mainViewModel.castLiveData.removeObserver(castStatusObserver)
                        mainViewModel.transportControls().sendCustomAction(Constants.ACTION_RESTORE_MEDIA_SESSION, null)
                    }
                }
            }

        })
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset > 0) {
            btnPlayPause.visibility = View.GONE
            progressBar.visibility = View.GONE
            btnCollapse.visibility = View.VISIBLE
        } else  progressBar.visibility = View.VISIBLE
    }


    override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) {
            btnPlayPause.visibility = View.GONE
            btnCollapse.visibility = View.VISIBLE

            //disable expanded controls when casting as we dont support next/previous yet
            if (isCasting) (activity as MainActivity).collapseBottomSheet()

        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            btnPlayPause.visibility = View.VISIBLE
            btnCollapse.visibility = View.GONE
        }
    }

    override fun onStop() {
        progressBar.disconnectController()
        progressText.disconnectController()
        seekBar.disconnectController()
        super.onStop()
    }
}
