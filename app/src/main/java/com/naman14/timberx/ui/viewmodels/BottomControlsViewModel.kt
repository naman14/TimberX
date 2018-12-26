package com.naman14.timberx.ui.viewmodels

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.toSongList
import com.naman14.timberx.vo.MediaData
import com.naman14.timberx.vo.Song

class BottomControlsViewModel: ViewModel() {

    var currentQueueLiveData = MutableLiveData<List<Song>>()
    var currentQueueMetaData = MutableLiveData<QueueEntity>()
    var currentData = MutableLiveData<MediaData>()

    fun getCurrentDataFromDB(context: Context): MediatorLiveData<MediaData> {

        currentData.value = MediaData()

        val mediator = MediatorLiveData<MediaData>()

        mediator.addSource(TimberDatabase.getInstance(context)!!.queueDao().getQueueSongs()) { songEntityList ->
            currentQueueLiveData.postValue(songEntityList?.toSongList(context))
        }

        mediator.addSource(TimberDatabase.getInstance(context)!!.queueDao().getQueueData()) { queueEntity ->
            if (queueEntity != null) {
                currentQueueMetaData.postValue(queueEntity)

                queueEntity.currentId?.let {currentId ->
                    val song = SongsRepository.getSongForId(context, currentId)
                    val mediaData = currentData.value?.fromDBData(song, queueEntity)
                    currentData.postValue(mediaData)
                    mediator.postValue(mediaData)
                }

            }
        }

        return mediator
    }

}