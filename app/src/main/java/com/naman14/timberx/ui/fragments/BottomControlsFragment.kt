package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.os.Handler
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
import com.naman14.timberx.ui.widgets.BottomSheetListener
import com.naman14.timberx.util.*
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*

class BottomControlsFragment : NowPlayingFragment(), BottomSheetListener {

    var binding by AutoClearedValue<LayoutBottomsheetControlsBinding>(this)

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

        binding.let {
            nowPlayingViewModel.currentData.observe(this, Observer {})
            it.viewModel = nowPlayingViewModel
            it.setLifecycleOwner(this)
        }

        setupUI()
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

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (slideOffset > 0) {
            btnPlayPause.visibility = View.GONE
            progressBar.visibility = View.GONE
            btnCollapse.visibility = View.VISIBLE
        }
    }


    override fun onStateChanged(bottomSheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) {
            btnPlayPause.visibility = View.GONE
            progressBar.visibility = View.GONE
            btnCollapse.visibility = View.VISIBLE
        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            btnPlayPause.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
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
