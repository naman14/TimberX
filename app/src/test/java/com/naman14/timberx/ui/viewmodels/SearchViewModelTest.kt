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

import com.naman14.timberx.repository.AlbumRepository
import com.naman14.timberx.repository.ArtistRepository
import com.naman14.timberx.repository.SongsRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class SearchViewModelTest {
    val songsRepo = mockk<SongsRepository>()
    val albumRepo = mockk<AlbumRepository>()
    val artistRepo = mockk<ArtistRepository>()
    val tested = SearchViewModel(songsRepo, albumRepo, artistRepo)

    @Before
    fun setup() {
        Dispatchers.setMain(Dispatchers.IO)
    }

    @Test
    fun `given query when search called then searchSongs called with query and limit 10`() {
        // given
        val query = "song"
        // when
        tested.search(query)
        // then
        verify { songsRepo.getSongs(query, 10) }
    }

    @Test
    fun `given query when search called then searchAlbums called with query and limit 7`() {
        // given
        val query = "song"
        // when
        tested.search(query)
        // then
        verify { albumRepo.getAlbums(query, 7) }
    }

    @Test
    fun `given query when search called then searchArtists called with query and limit 7`() {
        // given
        val query = "song"
        // when
        tested.search(query)
        // then
        verify { artistRepo.getArtists(query, 7) }
    }
}
