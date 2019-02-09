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
import android.support.v4.media.session.PlaybackStateCompat
import android.util.AttributeSet
import android.view.animation.LinearInterpolator

import com.naman14.timberx.util.Utils

import androidx.appcompat.widget.AppCompatTextView

class MediaProgressTextView : AppCompatTextView {

    private var mMediaController: MediaControllerCompat? = null
    private var mControllerCallback: MediaProgressTextView.ControllerCallback? = null

    private var duration: Int = 0

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

            text = Utils.makeShortTimeString(context, (progress / 1000).toLong())

            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeToEnd = ((duration - progress) / state.playbackSpeed).toInt()

                if (timeToEnd > 0) {
                    mProgressAnimator?.cancel()
                    mProgressAnimator = ValueAnimator.ofInt(progress, duration)
                            .setDuration((timeToEnd / mDurationScale).toLong())
                    mProgressAnimator!!.interpolator = LinearInterpolator()
                    mProgressAnimator!!.addUpdateListener(this)
                    mProgressAnimator!!.start()
                }
            } else {

                text = Utils.makeShortTimeString(context, state.position / 1000)
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            val max = metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.toInt() ?: 0
            duration = max
            onPlaybackStateChanged(mMediaController?.playbackState)
        }

        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val animatedIntValue = valueAnimator.animatedValue as Int
            text = Utils.makeShortTimeString(context, (animatedIntValue / 1000).toLong())
        }
    }
}
