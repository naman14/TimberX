package com.naman14.timberx.ui.albumdetail

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData
import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.vo.Album
import com.naman14.timberx.vo.Song

class AlbumDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val albumSongs = MutableLiveData<ArrayList<Song>>()

    fun getAlbumSongs(albumId: Long): MutableLiveData<ArrayList<Song>> {
        albumSongs.value = SongsRepository.getSongsForAlbum(getApplication(), albumId)
        return albumSongs
    }
}
