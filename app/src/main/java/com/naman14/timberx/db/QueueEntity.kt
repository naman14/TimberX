package com.naman14.timberx.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.naman14.timberx.vo.Song

@Entity(tableName = "queue_meta_data")
data class QueueEntity(@PrimaryKey(autoGenerate = false) var id: Long? = 0,
                       @ColumnInfo(name = "current_pos") var currentPos: Int,
                       @ColumnInfo(name = "current_seek_pos") var currentSeekPos: Int,
                       @ColumnInfo(name = "repeat_mode") var repeatMode: Int,
                       @ColumnInfo(name = "shuffle_mode") var shuffleMode: Int) {
}

