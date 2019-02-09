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
package com.naman14.timberx.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.provider.Settings
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar

/**
 * SeekBar that can be used with a [MediaSessionCompat] to track and seek in playing
 * media.
 */

class MediaProgressBar : ProgressBar {

    private var mMediaController: MediaControllerCompat? = null
    private var mControllerCallback: ControllerCallback? = null

    private var mProgressAnimator: ValueAnimator? = null

    //get the global duration scale for animators, user may chane the duration scale from developer options
    //need to make sure our value animator doesn't change the duration scale
    private val mDurationScale = Settings.Global.getFloat(context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setMediaController(mediaController: MediaControllerCompat?) {
        if (mediaController != null) {
            mControllerCallback = ControllerCallback()
            mediaController.registerCallback(mControllerCallback!!)
            mControllerCallback!!.onMetadataChanged(mediaController.metadata)
            mControllerCallback!!.onPlaybackStateChanged(mediaController.playbackState)
        } else if (mMediaController != null) {
            mMediaController!!.unregisterCallback(mControllerCallback!!)
            mControllerCallback = null
        }
        mMediaController = mediaController
    }

    fun disconnectController() {
        if (mMediaController != null) {
            mMediaController!!.unregisterCallback(mControllerCallback!!)
            mControllerCallback = null
            mMediaController = null
        }
    }

    private inner class ControllerCallback : MediaControllerCompat.Callback(), ValueAnimator.AnimatorUpdateListener {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            state ?: return

            // If there's an ongoing animation, stop it now.
            if (mProgressAnimator != null) {
                mProgressAnimator!!.cancel()
                mProgressAnimator = null
            }

            val progress = state.position.toInt()
            setProgress(progress)

            // If the media is playing then the seekbar should follow it, and the easiest
            // way to do that is to create a ValueAnimator to update it so the bar reaches
            // the end of the media the same time as playback gets there (or close enough).

            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeToEnd = ((max - progress) / state.playbackSpeed).toInt()

                if (timeToEnd > 0) {
                    mProgressAnimator?.cancel()
                    mProgressAnimator = ValueAnimator.ofInt(progress, max)
                            .setDuration((timeToEnd / mDurationScale).toLong())
                    mProgressAnimator!!.interpolator = LinearInterpolator()
                    mProgressAnimator!!.addUpdateListener(this)
                    mProgressAnimator!!.start()
                }
            } else {
                setProgress(state.position.toInt())
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            val max = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt() ?: 0

            setMax(max)
            onPlaybackStateChanged(mMediaController?.playbackState)
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            // If the user is changing the slider, cancel the animation.
            val animatedIntValue = valueAnimator.animatedValue as Int
            progress = animatedIntValue
        }
    }
}
