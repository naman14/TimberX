package com.happyproject.blackpinkplay.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import com.afollestad.rxkprefs.Pref
import com.happyproject.blackpinkplay.PREF_APP_THEME
import com.happyproject.blackpinkplay.R
import com.happyproject.blackpinkplay.constants.AppThemes
import com.happyproject.blackpinkplay.extensions.attachLifecycle
import com.happyproject.blackpinkplay.extensions.toast
import com.happyproject.blackpinkplay.ui.activities.base.PermissionsActivity
import com.happyproject.blackpinkplay.ui.fragments.CheckSong
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import io.reactivex.functions.Consumer

class SplashActivity : PermissionsActivity() {

    private val appThemePref by inject<Pref<AppThemes>>(name = PREF_APP_THEME)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(appThemePref.get().themeRes)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (!permissionsManager.hasStoragePermission()) {
            permissionsManager.requestStoragePermission().subscribe(Consumer {
                checkSavedSong()
            }).attachLifecycle(this)
            return
        }

        checkSavedSong()
    }

    private fun goToMain() {
        val intent = Intent(this@SplashActivity, MainActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        startActivity(intent)
        finish()
    }

    private fun checkSavedSong() {
        val dir = File(Environment.getExternalStorageDirectory().toString() + "/" + CheckSong.PACKAGE_NAME)
        if (dir.exists() && dir.isDirectory) {
            val children = dir.listFiles()
            if (children.isNullOrEmpty()) {
                copy()
            } else {
                goToMain()
            }
        } else {
            toast("directory not found")
            dir.mkdirs()
            copy()
        }
    }

    private fun copy() {
        val bufferSize = 1024
        val assetManager = this.assets
        val assetFiles = assetManager.list("")

        assetFiles?.forEach {
            if (it.contains(".mp3")) {
                val inputStream = assetManager.open(it)
                val outputStream = FileOutputStream(
                    File(
                        Environment.getExternalStorageDirectory().toString() + "/" + CheckSong.PACKAGE_NAME,
                        it
                    )
                )

                try {
                    inputStream.copyTo(outputStream, bufferSize)
                } finally {
                    inputStream.close()
                    outputStream.flush()
                    outputStream.close()
                }
            }
        }

        Handler().postDelayed({
            goToMain()
        }, 3000)
    }
}
