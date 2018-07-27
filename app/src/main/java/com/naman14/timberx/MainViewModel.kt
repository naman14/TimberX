package com.naman14.timberx

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.util.toSong
import com.naman14.timberx.util.toSongList
import com.naman14.timberx.vo.Song

class MainViewModel(app: Application) : AndroidViewModel(app) {

    var currentSongLiveData =  MutableLiveData<Song>()
    var currentQueueLiveData = MutableLiveData<List<Song>>()
    var currentQueueMetaData = MutableLiveData<QueueEntity>()
    var progressLiveData = MutableLiveData<Int>()

    fun addObservers(): MediatorLiveData<Song> {

        val mediator = MediatorLiveData<Song>()

        val currentLiveData = TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueCurrentId()

        mediator.addSource(currentLiveData, {
            if (it != null) {
                val songEntity: SongEntity? = TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueSongById(it)
                if (songEntity != null) {
                    currentSongLiveData.postValue(songEntity.toSong())
                    mediator.postValue(songEntity.toSong())
                }
            }
        })


        mediator.addSource(TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueSongs(), {
            currentQueueLiveData.postValue(it?.toSongList())
        })

        mediator.addSource(TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueData(), {
            currentQueueMetaData.postValue(it)
            progressLiveData.postValue(it?.currentSeekPos)

        })

        return mediator

    }
}
