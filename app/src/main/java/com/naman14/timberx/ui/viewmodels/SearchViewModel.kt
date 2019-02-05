/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.naman14.timberx.models.Album
import com.naman14.timberx.models.Artist
import com.naman14.timberx.models.Song
import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.ArtistRepository
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.viewmodels.base.ScopedViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class SearchViewModel(val context: Context) : ScopedViewModel(Main) {

    private val searchData = SearchData()
    private val _searchLiveData = MutableLiveData<SearchData>()

    val searchLiveData: LiveData<SearchData> = _searchLiveData

    fun search(query: String) {
        if (query.length >= 3) {
            launch {
                val songs = withContext(IO) {
                    SongsRepository.searchSongs(context, query, 10)
                }
                if (songs.isNotEmpty()) {
                    searchData.songs = songs.toMutableList()
                }
                _searchLiveData.postValue(searchData)
            }

            launch {
                val albums = withContext(IO) {
                    AlbumRepository.getAlbums(context, query, 7)
                }
                if (albums.isNotEmpty()) {
                    searchData.albums = albums.toMutableList()
                }
                _searchLiveData.postValue(searchData)
            }

            launch {
                val artists = withContext(IO) {
                    ArtistRepository.getArtists(context, query, 7)
                }
                if (artists.isNotEmpty()) {
                    searchData.artists = artists.toMutableList()
                }
                _searchLiveData.postValue(searchData)
            }
        } else {
            _searchLiveData.postValue(searchData.clear())
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SearchViewModel(context) as T
        }
    }

    data class SearchData(
        var songs: MutableList<Song> = mutableListOf(),
        var albums: MutableList<Album> = mutableListOf(),
        var artists: MutableList<Artist> = mutableListOf()
    ) {

        fun clear(): SearchData {
            songs.clear()
            albums.clear()
            artists.clear()
            return this
        }
    }
}
