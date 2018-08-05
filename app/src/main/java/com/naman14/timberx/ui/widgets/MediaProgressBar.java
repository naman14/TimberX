package com.naman14.timberx.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

/**
 * SeekBar that can be used with a {@link MediaSessionCompat} to track and seek in playing
 * media.
 */

public class MediaProgressBar extends ProgressBar {

    private MediaControllerCompat mMediaController;
    private ControllerCallback mControllerCallback;

    private boolean mIsTracking = false;

    private ValueAnimator mProgressAnimator;

    public MediaProgressBar(Context context) {
        super(context);
    }

    public MediaProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setMediaController(final MediaControllerCompat mediaController) {
        if (mediaController != null) {
            mControllerCallback = new ControllerCallback();
            mediaController.registerCallback(mControllerCallback);
        } else if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
        }
        mMediaController = mediaController;
    }

    public void disconnectController() {
        if (mMediaController != null) {
            mMediaController.unregisterCallback(mControllerCallback);
            mControllerCallback = null;
            mMediaController = null;
        }
    }

    private class ControllerCallback
            extends MediaControllerCompat.Callback
            implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            // If there's an ongoing animation, stop it now.
            if (mProgressAnimator != null) {
                mProgressAnimator.cancel();
                mProgressAnimator = null;
            }

            final int progress = state != null
                    ? (int) state.getPosition()
                    : 0;
            setProgress(progress);

            // If the media is playing then the seekbar should follow it, and the easiest
            // way to do that is to create a ValueAnimator to update it so the bar reaches
            // the end of the media the same time as playback gets there (or close enough).
            if (state != null)  {
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    final int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());

                    if (timeToEnd > 0) {
                        mProgressAnimator = ValueAnimator.ofInt(progress, getMax())
                                .setDuration(timeToEnd);
                        mProgressAnimator.setInterpolator(new LinearInterpolator());
                        mProgressAnimator.addUpdateListener(this);
                        mProgressAnimator.start();
                    }
                } else if (state.getState() == PlaybackStateCompat.STATE_PAUSED
                        || state.getState() == PlaybackStateCompat.STATE_STOPPED){

                }
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            final int max = metadata != null
                    ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    : 0;

            setMax(max);
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator valueAnimator) {
            // If the user is changing the slider, cancel the animation.
            if (mIsTracking) {
                valueAnimator.cancel();
                return;
            }

            final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
            setProgress(animatedIntValue);
        }
    }
}