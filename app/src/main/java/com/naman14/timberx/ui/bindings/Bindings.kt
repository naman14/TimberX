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

import android.graphics.Bitmap
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.naman14.timberx.R
import com.naman14.timberx.util.Utils
import com.naman14.timberx.util.Utils.getAlbumArtUri

val IMAGE_ROUND_CORNERS_TRANSFORMER: Transformation<Bitmap>
    get() = RoundedCorners(2)

val LARGE_IMAGE_ROUND_CORNERS_TRANSFORMER: Transformation<Bitmap>
    get() = RoundedCorners(5)

val EXTRA_LARGE_IMAGE_ROUND_CORNERS_TRANSFORMER: Transformation<Bitmap>
    get() = RoundedCorners(8)

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, albumId: Long) {
    val size = view.resources.getDimensionPixelSize(R.dimen.album_art)
    val options = RequestOptions()
            .centerCrop()
            .override(size, size)
            .transform(IMAGE_ROUND_CORNERS_TRANSFORMER)
            .placeholder(R.drawable.ic_music_note)
    Glide.with(view)
            .load(getAlbumArtUri(albumId))
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(options)
            .into(view)
}

@BindingAdapter("playState")
fun setPlayState(view: ImageView, state: Int) {
    if (state == STATE_PLAYING) {
        view.setImageResource(R.drawable.ic_pause_outline)
    } else {
        view.setImageResource(R.drawable.ic_play_outline)
    }
}

@BindingAdapter("repeatMode")
fun setRepeatMode(view: ImageView, mode: Int) {
    when (mode) {
        REPEAT_MODE_NONE -> view.setImageResource(R.drawable.ic_repeat_none)
        REPEAT_MODE_ONE -> view.setImageResource(R.drawable.ic_repeat_one)
        REPEAT_MODE_ALL -> view.setImageResource(R.drawable.ic_repeat_all)
        else -> view.setImageResource(R.drawable.ic_repeat_none)
    }
}

@BindingAdapter("shuffleMode")
fun setShuffleMode(view: ImageView, mode: Int) {
    when (mode) {
        SHUFFLE_MODE_NONE -> view.setImageResource(R.drawable.ic_shuffle_none)
        SHUFFLE_MODE_ALL -> view.setImageResource(R.drawable.ic_shuffle_all)
        else -> view.setImageResource(R.drawable.ic_shuffle_none)
    }
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = Utils.makeShortTimeString(view.context, duration.toLong() / 1000)
}
