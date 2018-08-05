package com.naman14.timberx

import android.os.Bundle
import android.widget.RelativeLayout
import com.naman14.timberx.ui.main.MainFragment
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.databinding.MainActivityBinding
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.naman14.timberx.util.*

class MainActivity : MediaBrowserActivity() {

    private lateinit var viewModel: MainViewModel
    private var binding: MainActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) {
            replaceFragment(MainFragment.newInstance())
        }

        setupUI()
    }

    var controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            metadata?.let {
                viewModel.currentData.postValue(viewModel.currentData.value?.fromMediaMetadata(metadata))
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let {
                viewModel.currentData.postValue(viewModel.currentData.value?.fromPlaybackState(state))
            }
        }
    }

    override fun buildUIControls() {
        com.naman14.timberx.util.getMediaController(this)?.registerCallback(controllerCallback)
        progressBar.setMediaController(com.naman14.timberx.util.getMediaController(this))
        com.naman14.timberx.util.getMediaController(this)?.transportControls
                ?.sendCustomAction(Constants.ACTION_SET_MEDIA_STATE, null)
    }

    private fun setupUI() {

        val layoutParams = progressBar.layoutParams as RelativeLayout.LayoutParams
        progressBar.measure(0, 0)
        layoutParams.setMargins(0, -(progressBar.measuredHeight / 2), 0, 0)
        progressBar.layoutParams = layoutParams

        binding?.let {
            it.viewModel = viewModel
            it.setLifecycleOwner(this)
        }

        btnPlayPause.setOnClickListener {
            if (isPlaying(this)) {
                com.naman14.timberx.util.getMediaController(this)?.transportControls?.pause()
            } else {
                com.naman14.timberx.util.getMediaController(this)?.transportControls
                        ?.playFromMediaId(getCurrentMediaID(this)?.toString(),
                                getExtraBundle(getQueue(this)!!, "All songs", position(this)?.toInt()))
            }
        }

        viewModel.getCurrentDataFromDB().observe(this, Observer {

        })
    }

    override fun onStop() {
        super.onStop()
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback)
        }
        progressBar.disconnectController()
    }

}
