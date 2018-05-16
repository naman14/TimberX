package com.naman14.timberx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RelativeLayout
import com.naman14.timberx.ui.main.MainFragment
import kotlinx.android.synthetic.main.layout_bottomsheet_controls.*
import kotlinx.android.synthetic.main.main_activity.*
import android.content.Intent
import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import android.content.ServiceConnection
import com.naman14.timberx.repository.SongsRepository


class MainActivity : AppCompatActivity() {

//    private var musicService: TimberMusicService? = null
    private var playIntent: Intent? = null
    private var musicBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }

        val layoutParams = progressBar.layoutParams as RelativeLayout.LayoutParams
        progressBar.measure(0, 0)
        layoutParams.setMargins(0, -(progressBar.measuredHeight / 2), 0, 0)
        progressBar.layoutParams = layoutParams

    }


//    private val serviceConnection = object : ServiceConnection {
//
//        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            val binder = service as MusicBinder
//            musicService = binder.service
//
//            musicService?.setSongList(SongsRepository.loadSongs(this@MainActivity))
//            musicBound = true
//        }
//
//        override fun onServiceDisconnected(name: ComponentName) {
//            musicBound = false
//        }
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        if (playIntent == null) {
//            playIntent = Intent(this, TimberMusicService::class.java)
//            bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE)
//            startService(playIntent)
//        }
//    }
//
//    fun disconnectService() {
//        stopService(playIntent);
//        musicService = null
//    }
}
