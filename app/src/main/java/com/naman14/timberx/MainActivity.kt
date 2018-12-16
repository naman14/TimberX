package com.naman14.timberx

import android.os.Bundle
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.databinding.MainActivityBinding
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.View
import android.widget.LinearLayout
import com.naman14.timberx.util.*
import androidx.annotation.NonNull
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : MediaBrowserActivity() {

    private lateinit var viewModel: MainViewModel
    private var binding: MainActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        viewModel = ViewModelProviders
                .of(this, InjectorUtils.provideMainActivityViewModel(this))
                .get(MainViewModel::class.java)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel.rootMediaId.observe(this,
                Observer<String> { rootMediaId ->
                    if (rootMediaId != null) {
                        navigateToMediaItem(rootMediaId)
                    }
                })


        viewModel.navigateToMediaItem.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { mediaId ->
                navigateToMediaItem(mediaId)
            }
        })

        setupUI()
    }

    private fun navigateToMediaItem(mediaId: String) {
        var fragment: MediaItemFragment? = getBrowseFragment(mediaId)

        if (fragment == null) {
            fragment = MediaItemFragment.newInstance(mediaId)

            supportFragmentManager.beginTransaction()
                    .apply {
                        replace(R.id.container, fragment, mediaId)

                        // If this is not the top level media (root), we add it to the fragment
                        // back stack, so that actionbar toggle and Back will work appropriately:
                        if (!isRootId(mediaId)) {
                            addToBackStack(null)
                        }
                    }
                    .commit()
        }
    }


    private var controllerCallback: MediaControllerCompat.Callback = object : MediaControllerCompat.Callback() {
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
        progressText.setMediaController(com.naman14.timberx.util.getMediaController(this))
        seekBar.setMediaController(com.naman14.timberx.util.getMediaController(this))
        com.naman14.timberx.util.getMediaController(this)?.transportControls
                ?.sendCustomAction(Constants.ACTION_SET_MEDIA_STATE, null)
    }

    private fun setupUI() {

        val layoutParams = progressBar.layoutParams as LinearLayout.LayoutParams
        progressBar.measure(0, 0)
        layoutParams.setMargins(0, -(progressBar.measuredHeight / 2), 0, 0)
        progressBar.layoutParams = layoutParams
        song_title.isSelected = true

        binding?.let {
            it.viewModel = viewModel
            it.setLifecycleOwner(this)
        }

        val parentThatHasBottomSheetBehavior = bottom_sheet_parent as FrameLayout
        val mBottomSheetBehavior = BottomSheetBehavior.from(parentThatHasBottomSheetBehavior)
        if (mBottomSheetBehavior != null) {
            btnCollapse.setOnClickListener {
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            mBottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) {
                        btnPlayPause.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        btnCollapse.visibility = View.VISIBLE
                        dimOverlay.visibility = View.VISIBLE
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        btnPlayPause.visibility = View.VISIBLE
                        progressBar.visibility = View.VISIBLE
                        btnCollapse.visibility = View.GONE
                        dimOverlay.visibility = View.GONE
                    }
                }
                override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
                    if (slideOffset > 0) {
                        btnPlayPause.visibility = View.GONE
                        progressBar.visibility = View.GONE
                        btnCollapse.visibility = View.VISIBLE
                        dimOverlay.alpha = slideOffset
                    } else if (slideOffset == 0f) dimOverlay.visibility = View.GONE
                }
            })
        }

        viewModel.getCurrentDataFromDB(this).observe(this, Observer {
        })
    }

    fun togglePlayPause(v: View) {
        if (isPlaying(this)) {
            com.naman14.timberx.util.getMediaController(this)?.transportControls?.pause()
        } else {
            com.naman14.timberx.util.getMediaController(this)?.transportControls
                    ?.playFromMediaId(getCurrentMediaID(this)?.toString(),
                            getExtraBundle(getQueue(this)!!, "All songs", position(this)?.toInt()))
        }
    }

    override fun onStop() {
        if (MediaControllerCompat.getMediaController(this) != null) {
            MediaControllerCompat.getMediaController(this).unregisterCallback(controllerCallback)
        }
        progressBar.disconnectController()
        progressText.disconnectController()
        seekBar.disconnectController()
        super.onStop()

    }

    private fun isRootId(mediaId: String) = mediaId == viewModel.rootMediaId.value

    private fun getBrowseFragment(mediaId: String): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaId) as MediaItemFragment?
    }

}
