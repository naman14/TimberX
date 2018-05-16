package com.naman14.timberx.ui.queue

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.vo.Song

class QueueViewModel(app: Application) : AndroidViewModel(app) {

    private val queueSongs = MutableLiveData<ArrayList<SongEntity>>()

    fun getQueueSongs(): LiveData<List<SongEntity>> {
       return TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueSongs()

    }

    fun getQueueData(): LiveData<QueueEntity> {
        return TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueData()
    }
}