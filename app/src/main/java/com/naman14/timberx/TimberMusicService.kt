package com.naman14.timberx

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.media.MediaPlayer
import android.os.PowerManager
import com.naman14.timberx.TimberMusicService.MusicBinder
import android.content.ContextWrapper
import android.app.Activity
import android.content.Context
import android.content.ServiceConnection
import java.util.*
import android.content.ComponentName
import com.naman14.timberx.vo.Song


class TimberMusicService: Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    companion object {
        val mConnectionMap = WeakHashMap<Context, ServiceBinder>()
        var mService: TimberMusicService? = null
        var mServiceStartId: Int = -1

        fun bindToService(context: Context,
                          callback: ServiceConnection): ServiceToken? {

            var realActivity: Activity? = (context as Activity).parent
            if (realActivity == null) {
                realActivity = context
            }
            val contextWrapper = ContextWrapper(realActivity)
            contextWrapper.startService(Intent(contextWrapper, TimberMusicService::class.java))
            val binder = ServiceBinder(callback,
                    contextWrapper.applicationContext)
            if (contextWrapper.bindService(
                            Intent().setClass(contextWrapper, TimberMusicService::class.java!!), binder, 0)) {
                mConnectionMap.put(contextWrapper, binder)
                return ServiceToken(contextWrapper)
            }
            return null
        }

        fun unbindFromService(token: ServiceToken?) {
            if (token == null) {
                return
            }
            val mContextWrapper = token!!.mWrappedContext
            val mBinder = mConnectionMap.remove(mContextWrapper) ?: return
            mContextWrapper.unbindService(mBinder)
            if (mConnectionMap.isEmpty()) {
                mService = null
            }
        }

        fun isPlaybackServiceConnected(): Boolean {
            return mService != null
        }
    }

    private val musicBind = MusicBinder()
    private var player: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer()
        player?.setWakeMode(applicationContext,
                    PowerManager.PARTIAL_WAKE_LOCK)
        player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player?.setOnPreparedListener(this)
        player?.setOnCompletionListener(this)
        player?.setOnErrorListener(this)

    }

    override fun onBind(p0: Intent?): IBinder {
        return musicBind
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopSelf(mServiceStartId)
        return false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mServiceStartId = startId

        return START_STICKY

    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    inner class MusicBinder : Binder() {
        internal val service: TimberMusicService
            get() = this@TimberMusicService
    }

    override fun onPrepared(player: MediaPlayer?) {
        player?.start()
    }

    override fun onCompletion(player: MediaPlayer?) {

    }

    override fun onError(player: MediaPlayer?, p1: Int, p2: Int): Boolean {

        return false
    }


    fun playSong(song: Song) {
        player?.setDataSource(this, getSongUri(song.id))
        player?.prepareAsync()
    }

    class ServiceToken(var mWrappedContext: ContextWrapper)

    class ServiceBinder(private val mCallback: ServiceConnection?, private val mContext: Context) : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimberMusicService.MusicBinder
            mService = binder.service
            mCallback?.onServiceConnected(className, service)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mCallback?.onServiceDisconnected(className)
            mService = null
        }
    }
}