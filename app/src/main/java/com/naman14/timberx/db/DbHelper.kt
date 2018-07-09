package com.naman14.timberx.db

import android.content.Context
import com.naman14.timberx.util.doAsync
import com.naman14.timberx.util.toSong
import com.naman14.timberx.util.toSongEntityList
import com.naman14.timberx.vo.Song

object DbHelper {

    fun updateQueueSongs(context: Context, queueSongs: ArrayList<Song>, currentSongId: Long) {

        doAsync {
            val currentList: List<SongEntity>? = TimberDatabase.getInstance(context)!!.queueDao().getQueueSongsSync()
            val list: List<SongEntity> = queueSongs.toSongEntityList()

            if (queueSongs.size != 0 && currentList != null && !currentList.equals(list)) {
                TimberDatabase.getInstance(context)!!.queueDao().clearQueueSongs()
                TimberDatabase.getInstance(context)!!.queueDao().insertAllSongs(list)
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

    fun getCurrentSong(context: Context): Song {
        return TimberDatabase.getInstance(context)!!.queueDao()
                .getQueueSongById(TimberDatabase.getInstance(context)!!.queueDao().getQueueCurrentIdSync()).toSong()
    }
}