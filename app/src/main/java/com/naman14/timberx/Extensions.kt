package com.naman14.timberx

import android.content.ContentUris
import android.net.Uri

fun getService(): TimberMusicService? {
    return TimberMusicService.mService
}

fun getSongUri(id: Long): Uri {
   return ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id)
}