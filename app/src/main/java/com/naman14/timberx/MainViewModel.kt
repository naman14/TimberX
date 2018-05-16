package com.naman14.timberx

import android.app.Application
import androidx.lifecycle.*
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.util.toSong
import com.naman14.timberx.vo.Song

class MainViewModel(app: Application) : AndroidViewModel(app) {

    var currentSongLiveData =  MutableLiveData<Song>()

    fun getCurrentSong(): MediatorLiveData<Song> {

        val mediator = MediatorLiveData<Song>()

        val currentLiveData = TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueCurrentId()
        val queueLiveData = TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueSongs()

        mediator.addSource(currentLiveData, {
            if (it != null) {
                val songEntity: SongEntity? = TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueSongById(it)
                if (songEntity != null)
                 currentSongLiveData.postValue(songEntity.toSong())
            }
        })

        return mediator

    }
}
