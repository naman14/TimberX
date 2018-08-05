package com.naman14.timberx.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_meta_data")
data class QueueEntity(@PrimaryKey(autoGenerate = false) var id: Long = 0,
                       @ColumnInfo(name = "current_id") var currentId: Long? = 0,
                       @ColumnInfo(name = "current_seek_pos") var currentSeekPos: Long? = 0,
                       @ColumnInfo(name = "repeat_mode") var repeatMode: Int? = 0,
                       @ColumnInfo(name = "shuffle_mode") var shuffleMode: Int? = 0,
                       @ColumnInfo(name = "play_state") var playState: Int? = 0) {

}



