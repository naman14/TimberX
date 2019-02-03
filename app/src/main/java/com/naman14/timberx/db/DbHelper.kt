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

import android.content.Context
import com.naman14.timberx.util.doAsync
import com.naman14.timberx.util.toSongEntityList

object DbHelper {

    fun updateQueueSongs(context: Context, queueSongs: LongArray?, currentSongId: Long?) {
        if (queueSongs == null || currentSongId == null) {
            return
        }
        doAsync {
            val currentList: List<SongEntity>? = TimberDatabase.getInstance(context)!!.queueDao().getQueueSongsSync()
            val songListToSave: List<SongEntity>? = queueSongs.toSongEntityList(context)

            if (queueSongs.isNotEmpty() && currentList != null && !currentList.equals(songListToSave)) {
                TimberDatabase.getInstance(context)!!.queueDao().clearQueueSongs()
                TimberDatabase.getInstance(context)!!.queueDao().insertAllSongs(songListToSave!!)
                setCurrentSongId(context, currentSongId)
            } else {
                setCurrentSongId(context, currentSongId)
            }
        }.execute()
    }

    fun updateQueueData(context: Context, queueData: QueueEntity) {
        TimberDatabase.getInstance(context)!!.queueDao().insert(queueData)
    }

    fun setCurrentSongId(context: Context, id: Long) {
        TimberDatabase.getInstance(context)!!.queueDao().setCurrentId(id)
    }

    fun setCurrentSeekPos(context: Context, pos: Int) {
        TimberDatabase.getInstance(context)!!.queueDao().setCurrentSeekPos(pos)
    }

    fun setPlayState(context: Context, state: Int) {
        TimberDatabase.getInstance(context)!!.queueDao().setPlayState(state)
    }
}
