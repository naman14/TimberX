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
package com.naman14.timberx.ui.bindings

import android.view.View
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.naman14.timberx.R
import com.naman14.timberx.extensions.observeOnce
import com.naman14.timberx.network.Outcome
import com.naman14.timberx.network.api.LastFmRestService
import com.naman14.timberx.network.models.ArtworkSize
import com.naman14.timberx.network.models.ArtworkSize.MEGA
import com.naman14.timberx.network.models.ofSize
import org.koin.standalone.StandAloneContext
import timber.log.Timber

data class CacheKey(
    val artist: String,
    val album: String = "",
    val size: ArtworkSize
)

val imageUrlCache = mutableMapOf<CacheKey, String>()

@BindingAdapter("artistName", "artworkSize", requireAll = true)
fun setLastFmArtistImage(
    view: ImageView,
    artistName: String,
    artworkSize: ArtworkSize
) {
    Timber.d("""setLastFmArtistImage("$artistName", ${artworkSize.apiValue})""")
    val cacheKey = CacheKey(artistName, "", artworkSize)
    val cachedUrl = imageUrlCache[cacheKey]
    val resizeTo =
            view.px(if (artworkSize == MEGA) R.dimen.album_art_mega else R.dimen.album_art_large)
    val transformation = artworkSize.transformation()
    val options = RequestOptions()
            .centerCrop()
            .override(resizeTo, resizeTo)
            .transform(transformation)

    if (cachedUrl != null) {
        Glide.with(view)
                .load(cachedUrl)
                .apply(options)
                .into(view)
        return
    }

    fetchArtistImage(artistName, artworkSize, callback = { url ->
        if (url.isEmpty()) return@fetchArtistImage
        Glide.with(view)
                .load(url)
                .apply(options)
                .into(view)
    })
}

@BindingAdapter("albumArtist", "albumName", "artworkSize", requireAll = true)
fun setLastFmAlbumImage(
    view: ImageView,
    albumArtist: String,
    albumName: String,
    artworkSize: ArtworkSize
) {
    // TODO allow local albums to be default, avoid loading remote if unnecessary
    // TODO remote images should ideally be saved as permanent album art locally if none already exists.
    Timber.d("""setLastFmAlbumImage("$albumArtist", "$albumName", ${artworkSize.apiValue})""")
    val cacheKey = CacheKey(albumArtist, albumName, artworkSize)
    val cachedUrl = imageUrlCache[cacheKey]
    val resizeTo =
            view.px(if (artworkSize == MEGA) R.dimen.album_art_mega else R.dimen.album_art_large)
    val transformation = artworkSize.transformation()
    val options = RequestOptions()
            .centerCrop()
            .override(resizeTo, resizeTo)
            .transform(transformation)

    if (cachedUrl != null) {
        Glide.with(view)
                .load(cachedUrl)
                .apply(options)
                .into(view)
        return
    }

    fetchAlbumImage(albumArtist, albumName, artworkSize, callback = { url ->
        if (url.isEmpty()) return@fetchAlbumImage
        Glide.with(view)
                .load(url)
                .apply(options)
                .into(view)
    })
}

private fun fetchArtistImage(
    artistName: String,
    artworkSize: ArtworkSize,
    callback: (url: String) -> Unit
) {
    val lastFmService = StandAloneContext.getKoin()
            .koinContext.get<LastFmRestService>()
    val artistData = lastFmService.getArtistInfo(artistName)

    artistData.observeOnce {
        Timber.d("""getArtistInfo("$artistName") outcome: $it""")
        when (it) {
            is Outcome.Success -> {
                val artistResult = it.data.artist ?: return@observeOnce
                val url = artistResult.artwork.ofSize(artworkSize)
                        .url
                val cacheKey = CacheKey(artistName, "", artworkSize)
                imageUrlCache[cacheKey] = url
                Timber.d("""getArtistInfo("$artistName") image URL: $url""")
                callback(url)
            }
        }
    }
}

private fun fetchAlbumImage(
    artistName: String,
    albumName: String,
    artworkSize: ArtworkSize,
    callback: (url: String) -> Unit
) {
    val lastFmService = StandAloneContext.getKoin()
            .koinContext.get<LastFmRestService>()
    val albumData = lastFmService.getAlbumInfo(artistName, albumName)

    albumData.observeOnce {
        Timber.d("""getAlbumInfo("$albumName") outcome: $it""")
        when (it) {
            is Outcome.Success -> {
                val albumResult = it.data.album ?: return@observeOnce
                val url = albumResult.artwork.ofSize(artworkSize)
                        .url
                val cacheKey = CacheKey(artistName, albumName, artworkSize)
                imageUrlCache[cacheKey] = url
                Timber.d("""getAlbumInfo("$albumName") image URL: $url""")
                callback(url)
            }
        }
    }
}

private fun ArtworkSize.transformation() = if (this == MEGA) {
    EXTRA_LARGE_IMAGE_ROUND_CORNERS_TRANSFORMER
} else {
    LARGE_IMAGE_ROUND_CORNERS_TRANSFORMER
}

private fun View.px(@DimenRes dimen: Int) = resources.getDimensionPixelSize(dimen)
