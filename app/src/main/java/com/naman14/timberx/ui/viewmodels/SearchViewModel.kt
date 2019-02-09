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

import androidx.lifecycle.MutableLiveData
import com.naman14.timberx.models.Album
import com.naman14.timberx.models.Artist
import com.naman14.timberx.models.Song
import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.ArtistRepository
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.viewmodels.base.CoroutineViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext

class SearchViewModel(
    private val songsRepository: SongsRepository,
    private val albumsRepository: AlbumRepository,
    private val artistsRepository: ArtistRepository
) : CoroutineViewModel(Main) {

    private val searchData = SearchData()
    private val _searchLiveData = MutableLiveData<SearchData>()

    val searchLiveData = _searchLiveData

    fun search(query: String) {
        if (query.length >= 3) {
            launch {
                val songs = withContext(IO) {
                    songsRepository.searchSongs(query, 10)
                }
                if (songs.isNotEmpty()) {
                    searchData.songs = songs.toMutableList()
                }
                _searchLiveData.postValue(searchData)
            }

            launch {
                val albums = withContext(IO) {
                    albumsRepository.getAlbums(query, 7)
                }
                if (albums.isNotEmpty()) {
                    searchData.albums = albums.toMutableList()
                }
                _searchLiveData.postValue(searchData)
            }

            launch {
                val artists = withContext(IO) {
                    artistsRepository.getArtists(query, 7)
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
