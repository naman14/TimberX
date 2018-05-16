package com.naman14.timberx.vo

data class Song(var id: Long,
                var albumId: Long,
                var artistId: Long,
                var title: String,
                var artist: String,
                var album: String,
                var duration: Int,
                var trackNumber: Int)