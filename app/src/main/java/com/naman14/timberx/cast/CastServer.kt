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

    override fun serve(uri: String?, method: NanoHTTPD.Method?,
                       header: Map<String, String>?,
                       parameters: Map<String, String>?,
                       files: Map<String, String>?): NanoHTTPD.Response {
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