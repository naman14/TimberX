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
package com.naman14.timberx.extensions

import android.app.Activity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.naman14.timberx.constants.Constants

fun PlaybackStateCompat.position(): Long {
    return position
}

fun getMediaController(activity: Activity): MediaControllerCompat? {
    return MediaControllerCompat.getMediaController(activity)
}

fun getPlaybackState(activity: Activity): PlaybackStateCompat? {
    return getMediaController(activity)?.playbackState
}

fun isPlaying(activity: Activity): Boolean {
    return getMediaController(activity)?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING
}

fun position(activity: Activity): Long? {
    return getMediaController(activity)?.playbackState?.position
}

fun duration(activity: Activity): Long? {
    return getMediaController(activity)?.metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
}

fun MediaSessionCompat.position(): Long {
    return controller.playbackState.position
}

fun MediaSessionCompat.isPlaying(): Boolean {
    return controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING
}

fun getQueue(activity: Activity): LongArray? {
    return getMediaController(activity)?.queue?.toIDList()
}

fun getCurrentMediaID(activity: Activity): Long? {
    return getMediaController(activity)?.metadata?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)?.toLong()
}

fun getExtraBundle(queue: LongArray, title: String): Bundle? {
    return getExtraBundle(queue, title, 0)
}

fun getExtraBundle(queue: LongArray, title: String, seekTo: Int?): Bundle? {
    val bundle = Bundle()
    bundle.putLongArray(Constants.SONGS_LIST, queue)
    bundle.putString(Constants.QUEUE_TITLE, title)
    if (seekTo != null)
        bundle.putInt(Constants.SEEK_TO_POS, seekTo)
    else bundle.putInt(Constants.SEEK_TO_POS, 0)
    return bundle
}

fun ArrayList<MediaBrowserCompat.MediaItem>.toRawMediaItems(): ArrayList<MediaBrowserCompat.MediaItem> {
    val list = arrayListOf<MediaBrowserCompat.MediaItem>()
    forEach {
        list.add(MediaBrowserCompat.MediaItem(
                MediaDescriptionCompat.Builder()
                        .setMediaId(it.description.mediaId)
                        .setTitle(it.description.title)
                        .setIconUri(it.description.iconUri)
                        .setSubtitle(it.description.subtitle)
                        .build(), it.flags))
    }
    return list
}
