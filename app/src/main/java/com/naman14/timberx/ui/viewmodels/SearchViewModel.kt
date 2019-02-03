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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.naman14.timberx.models.Album
import com.naman14.timberx.models.Artist
import com.naman14.timberx.models.Song
import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.ArtistRepository
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.util.doAsyncPostWithResult

class SearchViewModel(val context: Context) : ViewModel() {

    private val searchData = SearchData()
    private val _searchLiveData = MutableLiveData<SearchData>()

    val searchLiveData = _searchLiveData

    fun search(query: String) {
        if (query.length >= 3) {
            doAsyncPostWithResult(handler = {
                SongsRepository.searchSongs(context, query, 10)
            }, postHandler = {
                if (it!!.isNotEmpty())
                    searchData.songs = ArrayList(it)
                _searchLiveData.postValue(searchData)
            }).execute()

            doAsyncPostWithResult(handler = {
                AlbumRepository.getAlbums(context, query, 7)
            }, postHandler = {
                if (it!!.isNotEmpty())
                    searchData.albums = ArrayList(it)
                _searchLiveData.postValue(searchData)
            }).execute()

            doAsyncPostWithResult(handler = {
                ArtistRepository.getArtists(context, query, 7)
            }, postHandler = {
                if (it!!.isNotEmpty())
                    searchData.artists = ArrayList(it)
                _searchLiveData.postValue(searchData)
            }).execute()
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
        var songs: ArrayList<Song> = arrayListOf(),
        var albums: ArrayList<Album> = arrayListOf(),
        var artists: ArrayList<Artist> = arrayListOf()
    ) {

        fun clear(): SearchData {
            songs.clear()
            albums.clear()
            artists.clear()
            return this
        }
    }
}
