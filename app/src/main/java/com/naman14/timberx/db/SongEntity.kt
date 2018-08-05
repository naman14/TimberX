package com.naman14.timberx.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_songs")
data class SongEntity(@PrimaryKey(autoGenerate = true) var uid: Int? = null,
                      @ColumnInfo(name = "id") var id: Long)