package com.naman14.timberx.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.palette.graphics.Palette
import com.naman14.timberx.MainActivity
import com.naman14.timberx.R
import com.squareup.picasso.Picasso
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver



object NotificationUtils {

    private const val CHANNEL_ID = "timberx_channel_01"
    private var mNotificationPostTime: Long = 0


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

    public fun buildNotification(context: Context, token: MediaSessionCompat.Token): Notification {

        val albumName = "Album name"
        val artistName = "Artist name"
        val isPlaying = false
        val text = if (TextUtils.isEmpty(albumName))
            artistName
        else
            artistName + " - " + albumName

        val playButtonResId = if (isPlaying)
            R.drawable.ic_pause_white
        else
            R.drawable.ic_play_arrow_white

        val nowPlayingIntent = Intent(context, MainActivity::class.java)
        val clickIntent = PendingIntent.getActivity(context, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        var artwork: Bitmap?
        artwork = Picasso.get().load(Utils.getAlbumArtUri(0)).get()

        if (artwork == null) {
            artwork = Picasso.get().load(R.drawable.icon).get()
        }

        if (mNotificationPostTime == 0L) {
            mNotificationPostTime = System.currentTimeMillis()
        }

        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        context, PlaybackStateCompat.ACTION_STOP)))
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(artwork)
                .setContentIntent(clickIntent)
                .setContentTitle("Album")
                .setContentText("Artist")
                .setSubText("Song name")
                .setColorized(true)
                .setShowWhen(false)
                .setWhen(mNotificationPostTime)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_STOP))
                .addAction(R.drawable.ic_previous_white,
                        "",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                .addAction(playButtonResId, "",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_PLAY_PAUSE))
                .addAction(R.drawable.ic_next_white,
                        "",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))


        if (artwork != null) {
            builder.setColor(Palette.from(artwork).generate().getVibrantColor(Color.parseColor("#403f4d")))
        }

        val n = builder.build()



        return n
    }
}