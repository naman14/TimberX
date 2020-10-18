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
package com.happyproject.blackpinkplay.ui.activities

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.os.Environment
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import com.happyproject.blackpinkplay.PREF_APP_THEME
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.constants.AppThemes
import com.happyproject.blackpinkplay.databinding.MainActivityBinding
import com.happyproject.blackpinkplay.extensions.addFragment
import com.happyproject.blackpinkplay.extensions.attachLifecycle
import com.happyproject.blackpinkplay.extensions.filter
import com.happyproject.blackpinkplay.extensions.hide
import com.happyproject.blackpinkplay.extensions.map
import com.happyproject.blackpinkplay.extensions.observe
import com.happyproject.blackpinkplay.extensions.replaceFragment
import com.happyproject.blackpinkplay.extensions.setDataBindingContentView
import com.happyproject.blackpinkplay.extensions.show
import com.happyproject.blackpinkplay.extensions.toast
import com.happyproject.blackpinkplay.models.MediaID
import com.happyproject.blackpinkplay.repository.SongsRepository
import com.happyproject.blackpinkplay.ui.activities.base.PermissionsActivity
import com.happyproject.blackpinkplay.ui.dialogs.DeleteSongDialog
import com.happyproject.blackpinkplay.ui.fragments.BottomControlsFragment
import com.happyproject.blackpinkplay.ui.fragments.CheckSong.PACKAGE_NAME
import com.happyproject.blackpinkplay.ui.fragments.MainFragment
import com.happyproject.blackpinkplay.ui.fragments.base.MediaItemFragment
import com.happyproject.blackpinkplay.ui.viewmodels.MainViewModel
import com.happyproject.blackpinkplay.ui.widgets.BottomSheetListener
import io.reactivex.functions.Consumer
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

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

        checkSavedSong()
        setupUI()
    }

    private fun checkSavedSong() {
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/" + PACKAGE_NAME)
        if (dir.exists() && dir.isDirectory) {
            val children = dir.listFiles()
            if (children.isEmpty()) {
                toast("songs empty")
                copy()
            }
        } else {
            toast("directory not found")
            dir.mkdirs()
            copy()
        }
    }

    private fun copy() {
        binding?.slidingLayout?.hide()
        binding?.containerDownload?.show()

        val bufferSize = 1024
        val assetManager = this.assets
        val assetFiles = assetManager.list("")

        assetFiles?.forEach {
            if (it.contains(".mp3")) {
                val inputStream = assetManager.open(it)
                val outputStream = FileOutputStream(
                    File(
                        Environment.getExternalStorageDirectory().toString() + "/" + PACKAGE_NAME, it
                    )
                )

                try {
                    inputStream.copyTo(outputStream, bufferSize)
                } finally {
                    inputStream.close()
                    outputStream.flush()
                    outputStream.close()
                }
            }
        }

        binding?.slidingLayout?.show()
        binding?.containerDownload?.hide()
        this.recreate()
    }

    private fun downloadSong() {
        val storage = Firebase.storage
        val listRef = storage.reference.child("audio")
        listRef.listAll()
            .addOnSuccessListener { (items, prefixes) ->
                prefixes.forEach { prefix ->
                    // All the prefixes under listRef.
                    // You may call listAll() recursively on them.
                }
                binding?.slidingLayout?.hide()
                binding?.containerDownload?.show()
                val totalSong = items.size

                items.forEachIndexed { index, item ->
                    // All the items under listRef.
                    // toast(item.name)
                }
                val itemsList = items
                val list = items

                // binding?.slidingLayout?.show()
                // binding?.containerDownload?.hide()
                // toast("Download completed")
            }
            .addOnFailureListener {
                toast("Failed when fetch songs")
                // Uh-oh, an error occurred!
            }
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
        val parentThatHasBottomSheetBehavior = binding?.bottomSheetParent as FrameLayout? ?: return

        bottomSheetBehavior = BottomSheetBehavior.from(parentThatHasBottomSheetBehavior)
        bottomSheetBehavior?.isHideable = true
        bottomSheetBehavior?.setBottomSheetCallback(BottomSheetCallback())

        binding?.dimOverlay?.setOnClickListener { collapseBottomSheet() }
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
                binding?.dimOverlay?.show()
            } else if (newState == STATE_COLLAPSED) {
                binding?.dimOverlay?.hide()
            }
            bottomSheetListener?.onStateChanged(bottomSheet, newState)
        }

        override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
            if (slideOffset > 0) {
                binding?.dimOverlay?.alpha = slideOffset
            } else if (slideOffset == 0f) {
                binding?.dimOverlay?.hide()
            }
            bottomSheetListener?.onSlide(bottomSheet, slideOffset)
        }
    }

    private fun isRootId(mediaId: MediaID) = mediaId.type == viewModel.rootMediaId.value?.type

    private fun getBrowseFragment(mediaId: MediaID): MediaItemFragment? {
        return supportFragmentManager.findFragmentByTag(mediaId.type) as MediaItemFragment?
    }
}
