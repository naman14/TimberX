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

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.naman14.timberx.R
import com.naman14.timberx.util.CircleTransform
import com.naman14.timberx.util.RoundedCornersTransformation
import com.naman14.timberx.util.Utils

import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

object ImageTransformation {
    private val radius = 2f //in dp
    private val margin = 0
    fun transformation(context: Context): Transformation = RoundedCornersTransformation(Utils.convertDpToPixel(radius, context).toInt(), margin)
}

object LargeImageTransformation {

    private val radius = 5f //in dp
    private val margin = 0

    fun transformation(context: Context): Transformation = RoundedCornersTransformation(Utils.convertDpToPixel(radius, context).toInt(), margin)
}

object ExtraLargeImageTransformation {

    private val radius = 8f //in dp
    private val margin = 0

    fun transformation(context: Context): Transformation = RoundedCornersTransformation(Utils.convertDpToPixel(radius, context).toInt(), margin)
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).centerCrop().resizeDimen(R.dimen.album_art, R.dimen.album_art).transform(ImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrlLarge")
fun setImageUrlLarge(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).centerCrop().resizeDimen(R.dimen.album_art_mega, R.dimen.album_art_mega).transform(LargeImageTransformation.transformation(view.context)).into(view)
}

@BindingAdapter("imageUrlNormal")
fun setImageUrlNormal(view: ImageView, albumId: Long) {
    Picasso.get().load(Utils.getAlbumArtUri(albumId)).error(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrl")
fun setImageUrl(view: ImageView, uri: String?) {
    if (uri != null && uri.isNotEmpty())
        Picasso.get().load(uri).centerCrop().resizeDimen(R.dimen.album_art, R.dimen.album_art).transform(ImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("imageUrlLarge")
fun setImageUrlLarge(view: ImageView, uri: String?) {
    if (uri != null && uri.isNotEmpty())
    Picasso.get().load(uri).centerCrop().resizeDimen(R.dimen.album_art_mega, R.dimen.album_art_mega).transform(ExtraLargeImageTransformation.transformation(view.context)).placeholder(R.drawable.ic_music_note).into(view)
}


@BindingAdapter("circleImageUrl")
fun setCircleImage(view: ImageView, uri: String) {
    if (uri.isNotEmpty())
        Picasso.get().load(uri).centerCrop().resizeDimen(R.dimen.album_art_circle_small, R.dimen.album_art_circle_small).transform(CircleTransform()).placeholder(R.drawable.ic_music_note).into(view)
}

@BindingAdapter("playState")
fun setPlayState(view: ImageView, state: Int) {
    if (state == PlaybackStateCompat.STATE_PLAYING) {
        view.setImageResource(R.drawable.ic_pause_outline)
    } else {
        view.setImageResource(R.drawable.ic_play_outline)
    }
}

@BindingAdapter("repeatMode")
fun setRepeatMode(view: ImageView, mode: Int) {
    when (mode) {
        PlaybackStateCompat.REPEAT_MODE_NONE -> view.setImageResource(R.drawable.ic_repeat_none)
        PlaybackStateCompat.REPEAT_MODE_ONE -> view.setImageResource(R.drawable.ic_repeat_one)
        PlaybackStateCompat.REPEAT_MODE_ALL -> view.setImageResource(R.drawable.ic_repeat_all)
        else -> view.setImageResource(R.drawable.ic_repeat_none)

    }
}

@BindingAdapter("shuffleMode")
fun setShuffleMode(view: ImageView, mode: Int) {
    when (mode) {
        PlaybackStateCompat.SHUFFLE_MODE_NONE -> view.setImageResource(R.drawable.ic_shuffle_none)
        PlaybackStateCompat.SHUFFLE_MODE_ALL -> view.setImageResource(R.drawable.ic_shuffle_all)
        else -> view.setImageResource(R.drawable.ic_shuffle_none)
    }
}

@BindingAdapter("duration")
fun setDuration(view: TextView, duration: Int) {
    view.text = Utils.makeShortTimeString(view.context, duration.toLong() / 1000)
}