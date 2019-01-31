package com.naman14.timberx.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
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

    private val mIsTracking = false

    private var mProgressAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    fun setMediaController(mediaController: MediaControllerCompat?) {
        if (mediaController != null) {
            mControllerCallback = ControllerCallback()
            mediaController.registerCallback(mControllerCallback!!)
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

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
        }

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
                            .setDuration(timeToEnd.toLong())
                    mProgressAnimator!!.interpolator = LinearInterpolator()
                    mProgressAnimator!!.addUpdateListener(this)
                    mProgressAnimator!!.start()
                }
            } else  {
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
            if (mIsTracking) {
                valueAnimator.cancel()
                return
            }

            val animatedIntValue = valueAnimator.animatedValue as Int
            progress = animatedIntValue
        }
    }
}