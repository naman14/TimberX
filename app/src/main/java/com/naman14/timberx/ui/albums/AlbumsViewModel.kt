package com.naman14.timberx.ui.albums

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData
import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.vo.Album
import com.naman14.timberx.vo.Song

class AlbumsViewModel(app: Application) : AndroidViewModel(app) {

    private val albums = MutableLiveData<ArrayList<Album>>()

    fun getAlbums(): MutableLiveData<ArrayList<Album>> {
        albums.value = AlbumRepository.getAllAlbums(getApplication())
        return albums
    }
}
