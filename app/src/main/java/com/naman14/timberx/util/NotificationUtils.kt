package com.naman14.timberx.util

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.palette.graphics.Palette
import com.naman14.timberx.MainActivity
import com.naman14.timberx.R
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import android.graphics.BitmapFactory
import android.support.v4.media.MediaMetadataCompat

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
            mChannel.setShowBadge(false);
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
            R.drawable.ic_play

        val nowPlayingIntent = Intent(context, MainActivity::class.java)
        val clickIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (mNotificationPostTime == 0L) {
            mNotificationPostTime = System.currentTimeMillis()
        }

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowCancelButton(true)
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
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)))
                .addAction(NotificationCompat.Action(playButtonResId, "",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .addAction(NotificationCompat.Action(R.drawable.ic_next,
                        "",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT)))


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
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(NOTIFICATION_ID, buildNotification(context, mediaSession))
    }
}