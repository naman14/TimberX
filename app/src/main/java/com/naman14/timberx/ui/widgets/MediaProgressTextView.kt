package com.naman14.timberx.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
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

    private val mIsTracking = false
    private var duration: Int = 0

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

            text = Utils.makeShortTimeString(context, (progress / 1000).toLong())

            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                val timeToEnd = ((duration - progress) / state.playbackSpeed).toInt()

                if (timeToEnd > 0) {
                    mProgressAnimator?.cancel()
                    mProgressAnimator = ValueAnimator.ofInt(progress, duration)
                            .setDuration(timeToEnd.toLong())
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
