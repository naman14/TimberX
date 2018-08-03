package com.naman14.timberx.util

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import com.naman14.timberx.R

object Utils {

    fun getAlbumArtUri(albumId: Long): Uri {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId)
    }

    fun makeShortTimeString(context: Context, _secs: Long): String {
        var secs = _secs
        val hours: Long
        val mins: Long

        hours = secs / 3600
        secs %= 3600
        mins = secs / 60
        secs %= 60

        val durationFormat = context.getResources().getString(
                if (hours == 0L) R.string.durationformatshort else R.string.durationformatlong)
        return String.format(durationFormat, hours, mins, secs)
    }

    fun isOreo(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun isMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    fun isLollipop(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    }


    enum class IdType private constructor(val mId: Int) {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);


        companion object {

            fun getTypeById(id: Int): IdType {
                for (type in values()) {
                    if (type.mId == id) {
                        return type
                    }
                }

                throw IllegalArgumentException("Unrecognized id: $id")
            }
        }
    }

    fun getEmptyAlbumArtUri(): String {
        return "android.resource://" + "com.naman14.timberx/drawable/icon"
    }
}