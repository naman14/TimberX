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
package com.naman14.timberx.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "queue_meta_data")
data class QueueEntity @Ignore constructor(
    @PrimaryKey(autoGenerate = false) var id: Long = 0,
    @ColumnInfo(name = "current_id") var currentId: Long? = 0,
    @ColumnInfo(name = "current_seek_pos") var currentSeekPos: Long? = 0,
    @ColumnInfo(name = "repeat_mode") var repeatMode: Int? = 0,
    @ColumnInfo(name = "shuffle_mode") var shuffleMode: Int? = 0,
    @ColumnInfo(name = "play_state") var playState: Int? = 0,
    @ColumnInfo(name = "queue_title") var queueTitle: String = "All songs"
) {
    constructor() : this(0, 0, 0, 0, 0, 0, "")
}
