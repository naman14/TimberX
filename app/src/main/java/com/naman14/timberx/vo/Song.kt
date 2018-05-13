package com.naman14.timberx.vo

data class Song(val id: Long,
                val albumId: Long,
                val artistId: Long,
                val title: String,
                val artist: String,
                val album: String,
                val duration: Int,
                val trackNumber: Int)