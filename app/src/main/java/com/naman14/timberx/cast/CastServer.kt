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
import android.net.Uri

import com.naman14.timberx.util.MusicUtils
import com.naman14.timberx.util.Constants
import com.naman14.timberx.util.Utils

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

import fi.iki.elonen.NanoHTTPD

class CastServer(private val context: Context) : NanoHTTPD(Constants.CAST_SERVER_PORT) {

    private var songUri: Uri? = null
    private var albumArtUri: Uri? = null

    override fun serve(
        uri: String?,
        method: NanoHTTPD.Method?,
        header: Map<String, String>?,
        parameters: Map<String, String>?,
        files: Map<String, String>?
    ): NanoHTTPD.Response {
        if (uri!!.contains("albumart")) {
            //serve the picture
            val albumId = parameters!!["id"]
            this.albumArtUri = Utils.getAlbumArtUri(java.lang.Long.parseLong(albumId))

            if (albumArtUri != null) {
                val mediasend = "image/jpg"
                var fisAlbumArt: InputStream? = null
                try {
                    fisAlbumArt = context.contentResolver.openInputStream(albumArtUri!!)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                val st = NanoHTTPD.Response.Status.OK

                //serve the song
                return NanoHTTPD.newChunkedResponse(st, mediasend, fisAlbumArt)
            }
        } else if (uri.contains("song")) {

            val songId = parameters!!["id"]
            this.songUri = MusicUtils.getSongUri(java.lang.Long.parseLong(songId))

            if (songUri != null) {
                val mediasend = "audio/mp3"
                var fisSong: FileInputStream? = null
                val song = File(MusicUtils.getRealPathFromURI(context, songUri!!))
                try {
                    fisSong = FileInputStream(song)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                val st = NanoHTTPD.Response.Status.OK

                //serve the song
                return NanoHTTPD.newFixedLengthResponse(st, mediasend, fisSong, song.length())
            }
        }
        return NanoHTTPD.newFixedLengthResponse("Error")
    }
}
