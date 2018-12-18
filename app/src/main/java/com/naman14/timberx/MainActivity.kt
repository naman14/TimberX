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
import androidx.appcompat.app.AppCompatActivity
import com.naman14.timberx.ui.main.BottomControlsFragment
import com.naman14.timberx.ui.main.MainFragment
import com.naman14.timberx.ui.widgets.BottomSheetListener
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var binding: MainActivityBinding? = null
    private var bottomSheetListener: BottomSheetListener? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

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
                        supportFragmentManager.beginTransaction()
                                .apply {
                                    replace(R.id.container, MainFragment.newInstance())
                                    replace(R.id.bottomControlsContainer, BottomControlsFragment.newInstance())
                                }
                                .commit()
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
                        if (!isRootId(mediaId)) {
                            addToBackStack(null)
                        }
                    }
                    .commit()
        }
    }

    private fun setupUI() {
        binding?.let {
            it.viewModel = viewModel
            it.setLifecycleOwner(this)
        }
        val parentThatHasBottomSheetBehavior = bottom_sheet_parent as FrameLayout

        bottomSheetBehavior = BottomSheetBehavior.from(parentThatHasBottomSheetBehavior)
        bottomSheetBehavior?.let { it.setBottomSheetCallback(BottomSheetCallback()) }

    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener
    }

    fun collapseBottomSheet() {
        btnCollapse.setOnClickListener {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) {
                dimOverlay.visibility = View.VISIBLE
            } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                dimOverlay.visibility = View.GONE
            }
            bottomSheetListener?.let { it.onStateChanged(bottomSheet, newState) }
        }

        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                dimOverlay.alpha = slideOffset
            } else if (slideOffset == 0f) dimOverlay.visibility = View.GONE
            bottomSheetListener?.let { it.onSlide(bottomSheet, slideOffset) }
        }
    }

    private fun isRootId(mediaId: String) = mediaId == viewModel.rootMediaId.value

    private fun getBrowseFragment(mediaId: String): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaId) as MediaItemFragment?
    }

}
