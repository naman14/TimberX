package com.naman14.timberx.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.databinding.MainActivityBinding
import android.view.MenuItem
import android.view.View
import com.naman14.timberx.util.*
import androidx.annotation.NonNull
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.mediarouter.app.MediaRouteButton
import com.naman14.timberx.ui.viewmodels.MainViewModel
import com.naman14.timberx.R
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.ui.fragments.BottomControlsFragment
import com.naman14.timberx.ui.fragments.MainFragment
import com.naman14.timberx.ui.fragments.MediaItemFragment
import com.naman14.timberx.ui.widgets.BottomSheetListener
import kotlinx.android.synthetic.main.main_activity.*
import androidx.core.app.NotificationCompat.getExtras
import com.naman14.timberx.models.Song
import android.provider.MediaStore
import com.naman14.timberx.repository.SongsRepository


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var binding: MainActivityBinding? = null
    private var bottomSheetListener: BottomSheetListener? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    private val storagePermission = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.main_activity)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (ContextCompat.checkSelfPermission(this@MainActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    storagePermission)
            return
        }

        setupUI()
    }

    private fun setupUI() {

        viewModel = ViewModelProviders
                .of(this, InjectorUtils.provideMainActivityViewModel(this))
                .get(MainViewModel::class.java)


        viewModel.rootMediaId.observe(this,
                Observer<MediaID> { rootMediaId ->
                    if (rootMediaId != null) {
                        supportFragmentManager.beginTransaction().replace(R.id.container, MainFragment.newInstance()).commit()
                        Handler().postDelayed({
                            supportFragmentManager.beginTransaction().replace(R.id.bottomControlsContainer, BottomControlsFragment.newInstance()).commit()
                        }, 150)
                        handleSearchIntent(intent)
                    }
                })


        viewModel.navigateToMediaItem.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { mediaId ->
                navigateToMediaItem(mediaId)
            }
        })

        binding?.let {
            it.viewModel = viewModel
            it.setLifecycleOwner(this)
        }
        val parentThatHasBottomSheetBehavior = bottom_sheet_parent as FrameLayout

        bottomSheetBehavior = BottomSheetBehavior.from(parentThatHasBottomSheetBehavior)
        bottomSheetBehavior?.isHideable = true
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior?.setBottomSheetCallback(BottomSheetCallback())
    }

    private fun navigateToMediaItem(mediaId: MediaID) {
        var fragment: MediaItemFragment? = getBrowseFragment(mediaId)

        if (fragment == null) {
            fragment = MediaItemFragment.newInstance(mediaId)

            supportFragmentManager.beginTransaction()
//                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .apply {
                        add(R.id.container, fragment, mediaId.type)
                        if (!isRootId(mediaId)) {
                            addToBackStack(null)
                        }
                    }
                    .commit()
        }
    }

    private fun handleSearchIntent(intent: Intent?) {
        if (intent == null) return

        if (intent.action != null && intent.action!!.equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            val songTitle = intent.extras!!.getString(MediaStore.EXTRA_MEDIA_TITLE, null)
            viewModel.transportControls().playFromSearch(songTitle, null)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            storagePermission -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setupUI()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener
    }

    fun collapseBottomSheet() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hideBottomSheet() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showBottomSheet() {
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_HIDDEN)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) {
                dimOverlay.visibility = View.VISIBLE
            } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                dimOverlay.visibility = View.GONE
            }
            bottomSheetListener?.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                dimOverlay.alpha = slideOffset
            } else if (slideOffset == 0f) dimOverlay.visibility = View.GONE
            bottomSheetListener?.onSlide(bottomSheet, slideOffset)
        }
    }

    private fun isRootId(mediaId: MediaID) = mediaId.type == viewModel.rootMediaId.value?.type

    private fun getBrowseFragment(mediaId: MediaID): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaId.type) as MediaItemFragment?
    }

    override fun onBackPressed() {
        bottomSheetBehavior?.let {
            if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                collapseBottomSheet()
            } else {
                super.onBackPressed()
            }
        }
    }

    fun setupCastButton(mediaRouteButton: MediaRouteButton) {
        viewModel.setupCastButton(mediaRouteButton)
    }

    override fun onResume() {
        viewModel.setupCastSession()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pauseCastSession()
    }
}
