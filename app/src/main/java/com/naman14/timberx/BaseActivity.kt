package com.naman14.timberx

import android.app.Activity
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.naman14.timberx.TimberMusicService.Companion.mService

open class BaseActivity: AppCompatActivity(), ServiceConnection {

    private var mToken: TimberMusicService.ServiceToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mToken = TimberMusicService.bindToService(this, this);

    }

    override fun onResume() {
        super.onResume()

        if(mService == null){
            mToken = TimberMusicService.bindToService(this, this);
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mToken != null) {
            TimberMusicService.unbindFromService(mToken)
            mToken = null
        }

    }

    override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {

    }

    override fun onServiceDisconnected(p0: ComponentName?) {

    }
}