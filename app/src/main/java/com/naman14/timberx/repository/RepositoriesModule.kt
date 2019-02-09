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
package com.naman14.timberx.repository

import com.naman14.timberx.PREF_ALBUM_SORT_ORDER
import com.naman14.timberx.PREF_SONG_SORT_ORDER
import org.koin.dsl.module.module

val repositoriesModule = module {

    factory {
        RealSongsRepository(get(), get(name = PREF_SONG_SORT_ORDER))
    } bind SongsRepository::class

    factory {
        RealAlbumRepository(get(), get(name = PREF_ALBUM_SORT_ORDER))
    } bind AlbumRepository::class

    factory {
        RealArtistRepository(get())
    } bind ArtistRepository::class

    factory {
        RealGenreRepository(get())
    } bind GenreRepository::class

    factory {
        RealPlaylistRepository(get())
    } bind PlaylistRepository::class

    factory {
        RealFoldersRepository()
    } bind FoldersRepository::class
}
