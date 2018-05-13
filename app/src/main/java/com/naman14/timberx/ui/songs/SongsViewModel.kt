package com.naman14.timberx.ui.songs

import android.content.Context
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.vo.Song

class SongsViewModel : ViewModel() {

    private val songs = MutableLiveData<ArrayList<Song>>()

    fun getSongs(context: Context): MutableLiveData<ArrayList<Song>>? {
        songs.value = SongsRepository.loadSongs(context)
        return songs
    }
}
