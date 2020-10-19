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
package com.happyproject.blackpinkplay.ui.fragments.album

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.constants.Constants.ALBUM
import com.happyproject.blackpinkplay.databinding.FragmentAlbumDetailBinding
import com.happyproject.blackpinkplay.extensions.addOnItemClick
import com.happyproject.blackpinkplay.extensions.argument
import com.happyproject.blackpinkplay.extensions.filter
import com.happyproject.blackpinkplay.extensions.getExtraBundle
import com.happyproject.blackpinkplay.extensions.inflateWithBinding
import com.happyproject.blackpinkplay.extensions.observe
import com.happyproject.blackpinkplay.extensions.safeActivity
import com.happyproject.blackpinkplay.extensions.toSongIds
import com.happyproject.blackpinkplay.models.Album
import com.happyproject.blackpinkplay.ui.adapters.SongsAdapter
import com.happyproject.blackpinkplay.ui.fragments.CheckSong
import com.happyproject.blackpinkplay.ui.fragments.base.MediaItemFragment
import com.happyproject.blackpinkplay.util.AutoClearedValue

class AlbumDetailFragment : MediaItemFragment() {
    private lateinit var songsAdapter: SongsAdapter
    lateinit var album: Album
    var binding by AutoClearedValue<FragmentAlbumDetailBinding>(this)

    private lateinit var adView: AdView

    private val adAdaptiveSize: AdSize
        get() {
            val display = activity?.windowManager?.defaultDisplay
            val outMetrics = DisplayMetrics()
            display?.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        album = argument(ALBUM)
        binding = inflater.inflateWithBinding(R.layout.fragment_album_detail, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        MobileAds.initialize(context)
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(getString(R.string.ads_device)))
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        loadBanner()

        binding.album = album

        songsAdapter = SongsAdapter(this).apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(safeActivity)
            adapter = songsAdapter
            addOnItemClick { position: Int, _: View ->
                val extras = getExtraBundle(songsAdapter.songs.toSongIds(), album.title)
                mainViewModel.mediaItemClicked(songsAdapter.songs[position], extras)
            }
        }

        mediaItemFragmentViewModel.mediaItems
            .filter { it.isNotEmpty() }
            .observe(this) { list ->
                @Suppress("UNCHECKED_CAST")
                songsAdapter.updateData(CheckSong.getValidSong(list))

                binding.songCountText.text = requireActivity().resources.getQuantityString(
                    R.plurals.number_songs,
                    CheckSong.getValidSong(list).size,
                    CheckSong.getValidSong(list).size
                )
            }
    }

    private fun loadBanner() {
        adView = AdView(context)
        binding.adViewContainer.addView(adView)
        adView.apply {
            adUnitId = getString(R.string.test_ads_adaptive)
            adSize = adAdaptiveSize
            loadAd(AdRequest.Builder().build())
        }
    }
}
