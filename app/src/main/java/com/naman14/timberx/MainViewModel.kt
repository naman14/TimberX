package com.naman14.timberx

import android.app.Application
import androidx.lifecycle.*
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.toSongList
import com.naman14.timberx.vo.MediaData
import com.naman14.timberx.vo.Song

class MainViewModel(app: Application) : AndroidViewModel(app) {

    var currentQueueLiveData = MutableLiveData<List<Song>>()
    var currentQueueMetaData = MutableLiveData<QueueEntity>()
    var currentData = MutableLiveData<MediaData>()

    fun getCurrentDataFromDB(): MediatorLiveData<MediaData> {

        currentData.value = MediaData()

        val mediator = MediatorLiveData<MediaData>()

//        mediator.addSource(TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueSongs(), {
//            currentQueueLiveData.postValue(it?.toSongList(getApplication()))
//        })
//
//        mediator.addSource(TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueData(), {
//            if (it != null) {
//                currentQueueMetaData.postValue(it)
//
//                it.currentId?.let {currentId ->
//                    val song = SongsRepository.getSongForId(getApplication(), currentId)
//                    val mediaData = currentData.value?.fromDBData(song, it)
//                    currentData.postValue(mediaData)
//                    mediator.postValue(mediaData)
//                }
//
//            }
//        })

        return mediator
    }

}
