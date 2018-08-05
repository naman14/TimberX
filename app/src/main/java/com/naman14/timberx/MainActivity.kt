package com.naman14.timberx

import android.os.Bundle
import android.widget.RelativeLayout
import com.naman14.timberx.ui.main.MainFragment
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.databinding.MainActivityBinding
import com.naman14.timberx.util.replaceFragment
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import com.naman14.timberx.util.isPlaying
import com.naman14.timberx.util.position

class MainActivity : MediaBrowserActivity() {

    private lateinit var viewModel: MainViewModel
    private var binding: MainActivityBinding? = null
    private lateinit var mUpdateProgress: Runnable

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
                progressBar.postDelayed(mUpdateProgress, 10)
            }
        }
    }

    override fun buildUIControls() {
        com.naman14.timberx.util.getMediaController(this)?.registerCallback(controllerCallback)
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
                com.naman14.timberx.util.getMediaController(this)?.transportControls?.play()
            }
        }

        mUpdateProgress = object : Runnable {
            override fun run() {
                val playing = isPlaying(this@MainActivity)
                if (playing) {
                    val position = position(this@MainActivity)
                    viewModel.progressLiveData.postValue(position?.toInt())
                    progressBar.postDelayed(this, 10)
                } else progressBar.removeCallbacks(this)
            }
        }

        if (com.naman14.timberx.util.getMediaController(this) != null &&
                com.naman14.timberx.util.getMediaController(this)?.metadata != null) {
            viewModel.currentData.postValue(viewModel.currentData.value?.fromMediaController(this))
        } else {
            viewModel.getCurrentDataFromDB().observe(this, Observer { })
        }
    }

    override fun onStop() {
        super.onStop()
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback)
        }
    }

}
