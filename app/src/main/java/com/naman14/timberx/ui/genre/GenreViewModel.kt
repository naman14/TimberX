package com.naman14.timberx.ui.genre

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.vo.Song

class GenreViewModel(app: Application) : AndroidViewModel(app) {

    private val songs = MutableLiveData<ArrayList<Song>>()

    fun getSongs(): MutableLiveData<ArrayList<Song>> {
        songs.value = SongsRepository.loadSongs(getApplication())
        return songs
    }
}
