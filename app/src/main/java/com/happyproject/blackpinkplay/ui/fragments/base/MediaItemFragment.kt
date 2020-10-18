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
package com.happyproject.blackpinkplay.ui.fragments.base

import android.os.Bundle
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.MEDIA_CALLER
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.MEDIA_ID_ARG
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.MEDIA_TYPE_ARG
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ALBUM
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ALL_ALBUMS
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ALL_ARTISTS
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ALL_FOLDERS
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ALL_GENRES
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ALL_PLAYLISTS
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ALL_SONGS
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_ARTIST
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_GENRE
import com.happyproject.blackpinkplay.playback.TimberMusicService.Companion.TYPE_PLAYLIST
import com.happyproject.blackpinkplay.constants.Constants.ACTION_REMOVED_FROM_PLAYLIST
import com.happyproject.blackpinkplay.constants.Constants.ACTION_SONG_DELETED
import com.happyproject.blackpinkplay.constants.Constants.ALBUM
import com.happyproject.blackpinkplay.constants.Constants.ARTIST
import com.happyproject.blackpinkplay.constants.Constants.CATEGORY_SONG_DATA
import com.happyproject.blackpinkplay.extensions.argumentOrEmpty
import com.happyproject.blackpinkplay.extensions.map
import com.happyproject.blackpinkplay.extensions.observe
import com.happyproject.blackpinkplay.models.CategorySongData
import com.happyproject.blackpinkplay.models.Genre
import com.happyproject.blackpinkplay.models.MediaID
import com.happyproject.blackpinkplay.models.Playlist
import com.happyproject.blackpinkplay.ui.fragments.FolderFragment
import com.happyproject.blackpinkplay.ui.fragments.GenreFragment
import com.happyproject.blackpinkplay.ui.fragments.PlaylistFragment
import com.happyproject.blackpinkplay.ui.fragments.album.AlbumDetailFragment
import com.happyproject.blackpinkplay.ui.fragments.album.AlbumsFragment
import com.happyproject.blackpinkplay.ui.fragments.artist.ArtistDetailFragment
import com.happyproject.blackpinkplay.ui.fragments.artist.ArtistFragment
import com.happyproject.blackpinkplay.ui.fragments.songs.CategorySongsFragment
import com.happyproject.blackpinkplay.ui.fragments.songs.SongsFragment
import com.happyproject.blackpinkplay.ui.viewmodels.MediaItemFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

open class MediaItemFragment : BaseNowPlayingFragment() {

    protected lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    private lateinit var mediaType: String
    private var mediaId: String? = null
    private var caller: String? = null

    companion object {
        fun newInstance(mediaId: MediaID): MediaItemFragment {
            val args = Bundle().apply {
                putString(MEDIA_TYPE_ARG, mediaId.type)
                putString(MEDIA_ID_ARG, mediaId.mediaId)
                putString(MEDIA_CALLER, mediaId.caller)
            }
            return when (mediaId.type?.toInt()) {
                TYPE_ALL_SONGS -> SongsFragment().apply { arguments = args }
                TYPE_ALL_ALBUMS -> AlbumsFragment().apply { arguments = args }
                TYPE_ALL_PLAYLISTS -> PlaylistFragment().apply { arguments = args }
                TYPE_ALL_ARTISTS -> ArtistFragment().apply { arguments = args }
                TYPE_ALL_FOLDERS -> FolderFragment().apply { arguments = args }
                TYPE_ALL_GENRES -> GenreFragment().apply { arguments = args }
                TYPE_ALBUM -> AlbumDetailFragment().apply {
                    arguments = args.apply { putParcelable(ALBUM, mediaId.mediaItem) }
                }
                TYPE_ARTIST -> ArtistDetailFragment().apply {
                    arguments = args.apply { putParcelable(ARTIST, mediaId.mediaItem) }
                }
                TYPE_PLAYLIST -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Playlist).apply {
                            val data = CategorySongData(name, songCount, TYPE_PLAYLIST, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                TYPE_GENRE -> CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Genre).apply {
                            val data = CategorySongData(name, songCount, TYPE_GENRE, id)
                            putParcelable(CATEGORY_SONG_DATA, data)
                        }
                    }
                }
                else -> SongsFragment().apply {
                    arguments = args
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mediaType = argumentOrEmpty(MEDIA_TYPE_ARG)
        mediaId = argumentOrEmpty(MEDIA_ID_ARG)
        caller = argumentOrEmpty(MEDIA_CALLER)

        val mediaId = MediaID(mediaType, mediaId, caller)
        mediaItemFragmentViewModel = getViewModel { parametersOf(mediaId) }

        mainViewModel.customAction
                .map { it.getContentIfNotHandled() }
                .observe(this) {
                    when (it) {
                        ACTION_SONG_DELETED -> mediaItemFragmentViewModel.reloadMediaItems()
                        ACTION_REMOVED_FROM_PLAYLIST -> mediaItemFragmentViewModel.reloadMediaItems()
                    }
                }
    }
}
