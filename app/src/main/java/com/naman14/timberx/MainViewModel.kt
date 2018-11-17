package com.naman14.timberx

import android.app.Application
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.*
import com.naman14.timberx.db.QueueEntity
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.db.TimberDatabase
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.*
import com.naman14.timberx.vo.MediaData
import com.naman14.timberx.vo.Song

class MainViewModel(val app: Application, val mediaBrowser: MediaBrowserCompat) : AndroidViewModel(app) {

    var currentQueueLiveData = MutableLiveData<List<Song>>()
    var currentQueueMetaData = MutableLiveData<QueueEntity>()
    var currentData = MutableLiveData<MediaData>()

    fun getCurrentDataFromDB(): MediatorLiveData<MediaData> {

        currentData.value = MediaData()

        val mediator = MediatorLiveData<MediaData>()

        mediator.addSource(TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueSongs(), {
            currentQueueLiveData.postValue(it?.toSongList(getApplication()))
        })

        mediator.addSource(TimberDatabase.getInstance(getApplication())!!.queueDao().getQueueData(), {
            if (it != null) {
                currentQueueMetaData.postValue(it)

                it.currentId?.let {currentId ->
                    val song = SongsRepository.getSongForId(getApplication(), currentId)
                    val mediaData = currentData.value?.fromDBData(song, it)
                    currentData.postValue(mediaData)
                    mediator.postValue(mediaData)
                }

            }
        })

        return mediator
    }

    val rootMediaId: String = mediaBrowser.root

    /**
     * [navigateToMediaItem] acts as an "event", rather than state. [Observer]s
     * are notified of the change as usual with [LiveData], but only one [Observer]
     * will actually read the data. For more information, check the [Event] class.
     */
    val navigateToMediaItem: LiveData<Event<String>> get() = _navigateToMediaItem
    private val _navigateToMediaItem = MutableLiveData<Event<String>>()

    /**
     * This method takes a [MediaItemData] and routes it depending on whether it's
     * browsable (i.e.: it's the parent media item of a set of other media items,
     * such as an album), or not.
     *
     * If the item is browsable, handle it by sending an event to the Activity to
     * browse to it, otherwise play it.
     */
    fun mediaItemClicked(clickedItem: MediaBrowserCompat.MediaItem) {
        if (clickedItem.isBrowsable) {
            browseToItem(clickedItem)
        } else {
            playMedia(clickedItem)
        }
    }

    /**
     * This posts a browse [Event] that will be handled by the
     * observer in [MainActivity].
     */
    private fun browseToItem(mediaItem: MediaBrowserCompat.MediaItem) {
        _navigateToMediaItem.value = Event(mediaItem.mediaId!!)
    }

    /**
     * This method takes a [MediaItemData] and does one of the following:
     * - If the item is *not* the active item, then play it directly.
     * - If the item *is* the active item, check whether "pause" is a permitted command. If it is,
     *   then pause playback, otherwise send "play" to resume playback.
     */
    fun playMedia(mediaItem: MediaBrowserCompat.MediaItem) {
//        getMediaController(activity!!)?.transportControls?.playFromMediaId(mediaItem.mediaId,
//                getExtraBundle(adapter.songs!!.toSongIDs(), "All songs"))
    }

}
