/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore.EXTRA_MEDIA_TITLE
import android.provider.MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.NonNull
import androidx.mediarouter.app.MediaRouteButton
import com.afollestad.rxkprefs.Pref
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.naman14.timberx.PREF_APP_THEME
import com.naman14.timberx.R
import com.naman14.timberx.constants.AppThemes
import com.naman14.timberx.databinding.MainActivityBinding
import com.naman14.timberx.extensions.*
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.activities.base.PermissionsActivity
import com.naman14.timberx.ui.dialogs.DeleteSongDialog
import com.naman14.timberx.ui.fragments.BottomControlsFragment
import com.naman14.timberx.ui.fragments.MainFragment
import com.naman14.timberx.ui.fragments.base.MediaItemFragment
import com.naman14.timberx.ui.viewmodels.MainViewModel
import com.naman14.timberx.ui.widgets.BottomSheetListener
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.main_activity.bottom_sheet_parent
import kotlinx.android.synthetic.main.main_activity.dimOverlay
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : PermissionsActivity(), DeleteSongDialog.OnSongDeleted {

    private val viewModel by viewModel<MainViewModel>()
    private val songsRepository by inject<SongsRepository>()
    private val appThemePref by inject<Pref<AppThemes>>(name = PREF_APP_THEME)

    private var binding: MainActivityBinding? = null
    private var bottomSheetListener: BottomSheetListener? = null
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(appThemePref.get().themeRes)
        super.onCreate(savedInstanceState)
        binding = setDataBindingContentView(R.layout.main_activity)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (!permissionsManager.hasStoragePermission()) {
            permissionsManager.requestStoragePermission().subscribe(Consumer {
                setupUI()
            }).attachLifecycle(this)
            return
        }

        setupUI()
    }

    fun setBottomSheetListener(bottomSheetListener: BottomSheetListener) {
        this.bottomSheetListener = bottomSheetListener
    }

    fun collapseBottomSheet() {
        bottomSheetBehavior?.state = STATE_COLLAPSED
    }

    fun hideBottomSheet() {
        bottomSheetBehavior?.state = STATE_HIDDEN
    }

    fun showBottomSheet() {
        if (bottomSheetBehavior?.state == STATE_HIDDEN) {
            bottomSheetBehavior?.state = STATE_COLLAPSED
        }
    }

    override fun onBackPressed() {
        bottomSheetBehavior?.let {
            if (it.state == STATE_EXPANDED) {
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

    override fun onSongDeleted(songId: Long) {
        viewModel.onSongDeleted(songId)
    }

    private fun setupUI() {
        viewModel.rootMediaId.observe(this) {
            replaceFragment(fragment = MainFragment())
            Handler().postDelayed({
                replaceFragment(
                        R.id.bottomControlsContainer,
                        BottomControlsFragment()
                )
            }, 150)

            //handle playback intents, (search intent or ACTION_VIEW intent)
            handlePlaybackIntent(intent)
        }

        viewModel.navigateToMediaItem
                .map { it.getContentIfNotHandled() }
                .filter { it != null }
                .observe(this) { navigateToMediaItem(it!!) }

        binding?.let {
            it.viewModel = viewModel
            it.lifecycleOwner = this
        }
        val parentThatHasBottomSheetBehavior = bottom_sheet_parent as FrameLayout

        bottomSheetBehavior = BottomSheetBehavior.from(parentThatHasBottomSheetBehavior)
        bottomSheetBehavior?.isHideable = true
        bottomSheetBehavior?.setBottomSheetCallback(BottomSheetCallback())

        dimOverlay.setOnClickListener { collapseBottomSheet() }
    }

    private fun navigateToMediaItem(mediaId: MediaID) {
        if (getBrowseFragment(mediaId) == null) {
            val fragment = MediaItemFragment.newInstance(mediaId)
            addFragment(
                    fragment = fragment,
                    tag = mediaId.type,
                    addToBackStack = !isRootId(mediaId)
            )
        }
    }

    private fun handlePlaybackIntent(intent: Intent?) {
        if (intent == null || intent.action == null) return

        when (intent.action!!) {
            INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH -> {
                val songTitle = intent.extras?.getString(EXTRA_MEDIA_TITLE, null)
                viewModel.transportControls().playFromSearch(songTitle, null)
            }
            ACTION_VIEW -> {
                val path = getIntent().data?.path ?: return
                val song = songsRepository.getSongFromPath(path)
                viewModel.mediaItemClicked(song, null)
            }
        }
    }

    private inner class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
            if (newState == STATE_DRAGGING || newState == STATE_EXPANDED) {
                dimOverlay.show()
            } else if (newState == STATE_COLLAPSED) {
                dimOverlay.hide()
            }
            bottomSheetListener?.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                dimOverlay.alpha = slideOffset
            } else if (slideOffset == 0f) {
                dimOverlay.hide()
            }
            bottomSheetListener?.onSlide(bottomSheet, slideOffset)
        }
    }

    private fun isRootId(mediaId: MediaID) = mediaId.type == viewModel.rootMediaId.value?.type

    private fun getBrowseFragment(mediaId: MediaID): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaId.type) as MediaItemFragment?
    }
}
