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

import android.text.TextUtils
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Observer
import com.naman14.timberx.R
import com.naman14.timberx.network.Outcome
import com.naman14.timberx.network.api.LastFmDataHandler
import com.naman14.timberx.network.models.ArtistInfo
import com.squareup.picasso.Picasso

val imageUrls: HashMap<String, String> = hashMapOf()

@BindingAdapter("lastFMArtistImage")
fun setLastFMArtistImage(view: ImageView, artist: String?) {
    if (imageUrls.containsKey(artist) && !TextUtils.isEmpty(imageUrls[artist])) {
        Picasso.get().load(imageUrls[artist]).centerCrop()
                .resizeDimen(R.dimen.album_art_mega, R.dimen.album_art_mega)
                .transform(ExtraLargeImageTransformation.transformation(view.context)).into(view)
    } else {
        fetchArtistImage(artist, 2, callback = { url ->
            if (url.isNotEmpty())
                Picasso.get().load(url).centerCrop()
                        .resizeDimen(R.dimen.album_art_mega, R.dimen.album_art_mega)
                        .transform(ExtraLargeImageTransformation.transformation(view.context)).into(view)
        })
    }

}

@BindingAdapter("lastFMLargeArtistImage")
fun setLastFMLargeArtistImage(view: ImageView, artist: String?) {
    fetchArtistImage(artist, 4, callback = { url ->
        if (url.isNotEmpty())
            Picasso.get().load(url).into(view)
    })
}

private fun fetchArtistImage(artist: String?, imageSizeIndex: Int, callback: (url: String) -> Unit) {
    if (artist != null && artist.isNotEmpty()) {
        val artistData = LastFmDataHandler.lastfmRepository.getArtistInfo(artist)
        val observer: Observer<Outcome<ArtistInfo>> = object : Observer<Outcome<ArtistInfo>> {
            override fun onChanged(it: Outcome<ArtistInfo>?) {
                artistData.removeObserver(this)
                when (it) {
                    is Outcome.Success -> {
                        if (it.data.artist == null) return
                        val url = it.data.artist!!.artwork[imageSizeIndex].url
                        imageUrls[artist] = url
                        callback(url)
                    }
                }
            }
        }
        artistData.observeForever(observer)
    }
}