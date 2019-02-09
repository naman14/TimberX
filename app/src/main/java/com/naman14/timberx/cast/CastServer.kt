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
package com.naman14.timberx.cast

import android.content.Context
import com.naman14.timberx.util.MusicUtils.getRealPathFromURI
import com.naman14.timberx.util.MusicUtils.getSongUri
import com.naman14.timberx.util.Utils.getAlbumArtUri
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR
import fi.iki.elonen.NanoHTTPD.Response.Status.NOT_FOUND
import fi.iki.elonen.NanoHTTPD.Response.Status.OK
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

const val CAST_SERVER_PORT = 5050

class CastServer(private val context: Context) : NanoHTTPD(CAST_SERVER_PORT) {
    companion object {
        private const val MIME_TYPE_IMAGE = "image/jpg"
        private const val MIME_TYPE_AUDIO = "audio/mp3"
        private const val MIME_TYPE_TEXT = "text/plain"

        const val PART_ALBUM_ART = "albumart"
        const val PART_SONG = "song"
        const val PARAM_ID = "id"
    }

    override fun serve(
        uri: String?,
        method: NanoHTTPD.Method?,
        header: Map<String, String>?,
        parameters: Map<String, String>?,
        files: Map<String, String>?
    ): NanoHTTPD.Response {
        if (uri?.contains(PART_ALBUM_ART) == true) {
            // SERVE ALBUM ART
            val albumId = parameters?.get(PARAM_ID) ?: return errorResponse()
            val albumArtUri = getAlbumArtUri(albumId.toLong())
            var fisAlbumArt: InputStream? = null
            try {
                fisAlbumArt = context.contentResolver.openInputStream(albumArtUri)
            } catch (e: FileNotFoundException) {
                Timber.e(e, "Failed to read album art from $albumArtUri")
                return errorResponse(e.message)
            }
            return newChunkedResponse(OK, MIME_TYPE_IMAGE, fisAlbumArt)
        } else if (uri?.contains(PART_SONG) == true) {
            // SERVE AUDIO
            val songId = parameters?.get(PARAM_ID) ?: return errorResponse()
            val songUri = getSongUri(songId.toLong())
            val songPath = getRealPathFromURI(context, songUri)
            val song = File(songPath)

            var fisSong: FileInputStream? = null
            try {
                fisSong = FileInputStream(song)
            } catch (e: FileNotFoundException) {
                Timber.e(e, "Failed to read song from $songUri")
                return errorResponse(e.message)
            }
            return newFixedLengthResponse(OK, MIME_TYPE_AUDIO, fisSong, song.length())
        }

        // ELSE DEFAULT IS NOT_FOUND
        return newFixedLengthResponse(NOT_FOUND, MIME_TYPE_TEXT, "Not Found")
    }

    private fun errorResponse(message: String? = "Error"): Response {
        return newFixedLengthResponse(INTERNAL_ERROR, MIME_TYPE_TEXT, message ?: "Error")
    }
}
