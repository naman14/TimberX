package com.naman14.timberx.util

import android.app.Activity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import com.naman14.timberx.MediaBrowserActivity

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

fun getExtraBundle(queue: LongArray, title: String): Bundle?  {
    return getExtraBundle(queue, title, 0)
}

fun getExtraBundle(queue: LongArray, title: String, seekTo: Int?): Bundle?  {
    val bundle = Bundle()
    bundle.putLongArray(Constants.SONGS_LIST, queue)
    bundle.putString(Constants.QUEUE_TITLE, title)
    if (seekTo != null)
        bundle.putInt(Constants.SEEK_TO_POS, seekTo)
    else bundle.putInt(Constants.SEEK_TO_POS, 0)
    return bundle
}

fun Fragment.mediaBrowser(): MediaBrowserCompat {
    if (activity != null && activity is MediaBrowserActivity)
        return (activity as MediaBrowserActivity).getMediaBrowser()
    else {
        throw IllegalAccessException("Only a fragment that has parent MediaBrowserActivity can get the associated mediaBrowser")
    }
}