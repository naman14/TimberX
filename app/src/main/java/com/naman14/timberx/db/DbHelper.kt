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