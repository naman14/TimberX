package com.naman14.timberx.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_songs")
data class SongEntity(@PrimaryKey(autoGenerate = true) var uid: Int? = null,
                      @ColumnInfo(name = "id") var id: Long,
                @ColumnInfo(name = "albumId") var albumId: Long,
                @ColumnInfo(name = "artistId") var artistId: Long,
                @ColumnInfo(name = "title")  var title: String,
                @ColumnInfo(name = "artist") var artist: String,
                @ColumnInfo(name = "album") var album: String,
                @ColumnInfo(name = "duration") var duration: Int,
                @ColumnInfo(name = "trackNumber") var trackNumber: Int)