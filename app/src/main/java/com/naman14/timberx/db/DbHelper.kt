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
import com.naman14.timberx.extensions.toSongEntityList
import com.naman14.timberx.repository.SongsRepository

// TODO this class can go away in favor of an injectable class of some sort. QueueRepository? QueueHelper?
object DbHelper {

    fun updateQueueSongs(
        context: Context,
        queueSongs: LongArray?,
        currentSongId: Long?,
        songsRepository: SongsRepository
    ) {
        if (queueSongs == null || currentSongId == null) {
            return
        }
        doAsync {
            val currentList: List<SongEntity>? = TimberDatabase.getInstance(context)!!.queueDao().getQueueSongsSync()
            val songListToSave: List<SongEntity>? = queueSongs.toSongEntityList(songsRepository)

            if (queueSongs.isNotEmpty() && currentList != null && currentList != songListToSave) {
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

    private fun setCurrentSongId(context: Context, id: Long) {
        TimberDatabase.getInstance(context)!!.queueDao().setCurrentId(id)
    }
}
