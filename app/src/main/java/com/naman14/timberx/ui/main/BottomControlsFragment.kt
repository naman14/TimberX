package com.naman14.timberx.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.naman14.timberx.NowPlayingFragment
import com.naman14.timberx.R
import com.naman14.timberx.databinding.LayoutBottomsheetControlsBinding
import com.naman14.timberx.util.*
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*

class BottomControlsFragment: NowPlayingFragment() {

    var binding by AutoClearedValue<LayoutBottomsheetControlsBinding>(this)

    companion object {
        fun newInstance() = BottomControlsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.layout_bottomsheet_controls, container, false)

        return  binding.root
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

        btnTogglePlayPause.setOnClickListener {  nowPlayingViewModel.currentData.value?.let { mediaData ->
            mainActivityViewModel.mediaItemClicked(mediaData.toDummySong(), null)
        } }

        btnPlayPause.setOnClickListener {  nowPlayingViewModel.currentData.value?.let { mediaData ->
            mainActivityViewModel.mediaItemClicked(mediaData.toDummySong(), null)
        } }
    }

    //    fun buildUIControls() {
//        com.naman14.timberx.util.getMediaController(this)?.registerCallback(controllerCallback)
//        progressBar.setMediaController(com.naman14.timberx.util.getMediaController(this))
//        progressText.setMediaController(com.naman14.timberx.util.getMediaController(this))
//        seekBar.setMediaController(com.naman14.timberx.util.getMediaController(this))
//        com.naman14.timberx.util.getMediaController(this)?.transportControls
//                ?.sendCustomAction(Constants.ACTION_SET_MEDIA_STATE, null)
//    }

    override fun onStop() {
//        if (MediaControllerCompat.getMediaController(this) != null) {
//            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback)
//        }
        progressBar.disconnectController()
        progressText.disconnectController()
        seekBar.disconnectController()
        super.onStop()

    }
}
