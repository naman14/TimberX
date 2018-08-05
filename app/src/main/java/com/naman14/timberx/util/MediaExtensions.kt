package com.naman14.timberx.util

import android.app.Activity
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat

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

fun MediaSessionCompat.position(): Long {
    return controller.playbackState.position
}

fun MediaSessionCompat.isPlaying(): Boolean {
    return controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING
}
