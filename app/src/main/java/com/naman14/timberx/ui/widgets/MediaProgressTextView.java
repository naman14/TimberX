package com.naman14.timberx.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.naman14.timberx.util.Utils;

import androidx.appcompat.widget.AppCompatTextView;

public class MediaProgressTextView extends AppCompatTextView {

    private MediaControllerCompat mMediaController;
    private MediaProgressTextView.ControllerCallback mControllerCallback;

    private boolean mIsTracking = false;
    private int duration;

    private ValueAnimator mProgressAnimator;

    public MediaProgressTextView(Context context) {
        super(context);
    }

    public MediaProgressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaProgressTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setMediaController(final MediaControllerCompat mediaController) {
        if (mediaController != null) {
            mControllerCallback = new MediaProgressTextView.ControllerCallback();
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

            setText(Utils.INSTANCE.makeShortTimeString(getContext(), progress / 1000));

            if (state == null) return;
            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                final int timeToEnd = (int) ((duration - progress) / state.getPlaybackSpeed());

                if (timeToEnd > 0) {
                    mProgressAnimator = ValueAnimator.ofInt(progress, duration)
                            .setDuration(timeToEnd);
                    mProgressAnimator.setInterpolator(new LinearInterpolator());
                    mProgressAnimator.addUpdateListener(this);
                    mProgressAnimator.start();
                }
            } else if (state.getState() == PlaybackStateCompat.STATE_PAUSED
                    || state.getState() == PlaybackStateCompat.STATE_STOPPED) {

                setText(Utils.INSTANCE.makeShortTimeString(getContext(), state.getPosition() / 1000));

            }

        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);

            final int max = metadata != null
                    ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                    : 0;

            duration = max;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator valueAnimator) {
            final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
            setText(Utils.INSTANCE.makeShortTimeString(getContext(), animatedIntValue / 1000));
        }
    }
}
