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

package com.naman14.timberx.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.palette.graphics.Palette
import com.naman14.timberx.ui.activities.MainActivity
import com.naman14.timberx.R
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import android.support.v4.media.MediaMetadataCompat
import com.naman14.timberx.TimberMusicService
import android.app.PendingIntent
import com.naman14.timberx.util.media.isPlaying


object NotificationUtils {

    private const val CHANNEL_ID = "timberx_channel_01"
    private var mNotificationPostTime: Long = 0
    private val NOTIFICATION_ID = 888


    private fun createNotificationChannel(context: Context) {
        if (Utils.isOreo()) {
            val name = "Media playback"
            val description = "Media playback controls"

            val importance = NotificationManager.IMPORTANCE_LOW
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = description
            mChannel.setShowBadge(false)
            mChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            manager!!.createNotificationChannel(mChannel)
        }
    }

    fun buildNotification(context: Context, mediaSession: MediaSessionCompat): Notification {

        if (mediaSession.controller.metadata == null || mediaSession.controller.playbackState == null) {
            return getEmptyNotification(context)
        }

        val albumName = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
        val artistName = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        val trackName = mediaSession.controller.metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
        val artwork = mediaSession.controller.metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART)

        val isPlaying = mediaSession.isPlaying()


        val playButtonResId = if (isPlaying)
            R.drawable.ic_pause
        else
            R.drawable.ic_play_outline

        val nowPlayingIntent = Intent(context, MainActivity::class.java)
        val clickIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val actionIntent = Intent(context, TimberMusicService::class.java)


        if (mNotificationPostTime == 0L) {
            mNotificationPostTime = System.currentTimeMillis()
        }

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0, 1, 2)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        context, PlaybackStateCompat.ACTION_STOP)))
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(artwork)
                .setContentIntent(clickIntent)
                .setContentTitle(trackName)
                .setContentText(artistName)
                .setSubText(albumName)
                .setColorized(true)
                .setShowWhen(false)
                .setWhen(mNotificationPostTime)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_STOP))
                .addAction(NotificationCompat.Action(R.drawable.ic_previous,
                        "",
                        PendingIntent.getService(context, 0, actionIntent.apply { action =  Constants.ACTION_PREVIOUS }, 0)))
                .addAction(NotificationCompat.Action(playButtonResId, "",
                        PendingIntent.getService(context, 0, actionIntent.apply { action =  Constants.ACTION_PLAY_PAUSE }, 0)))
                .addAction(NotificationCompat.Action(R.drawable.ic_next,
                        "",
                        PendingIntent.getService(context, 0, actionIntent.apply { action =  Constants.ACTION_NEXT }, 0)))


        if (artwork != null) {
            builder.setColor(Palette.from(artwork).generate().getVibrantColor(Color.parseColor("#403f4d")))
        }

        val n = builder.build()

        return n
    }

    fun getEmptyNotification(context: Context): Notification {
        createNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("TimberX")
                .setColorized(true)
                .setShowWhen(false)
                .setWhen(mNotificationPostTime)
                .setVisibility(Notification.VISIBILITY_PUBLIC)

        val n = builder.build()
        return n
    }

    fun updateNotification(context: Context, mediaSession: MediaSessionCompat) {
        doAsync {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .notify(NOTIFICATION_ID, buildNotification(context, mediaSession))
        }.execute()

    }
}