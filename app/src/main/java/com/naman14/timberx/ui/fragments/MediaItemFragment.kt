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
package com.naman14.timberx.ui.fragments

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.models.CategorySongData
import com.naman14.timberx.models.Genre
import com.naman14.timberx.models.MediaID
import com.naman14.timberx.models.Playlist
import com.naman14.timberx.ui.viewmodels.MediaItemFragmentViewModel
import com.naman14.timberx.util.Constants
import com.naman14.timberx.util.InjectorUtils

open class MediaItemFragment : BaseNowPlayingFragment() {

    private lateinit var mediaType: String
    lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    private var mediaId: String? = null
    private var caller: String? = null

    companion object {
        fun newInstance(mediaId: MediaID): MediaItemFragment {

            val args = Bundle().apply {
                putString(TimberMusicService.MEDIA_TYPE_ARG, mediaId.type)
                putString(TimberMusicService.MEDIA_ID_ARG, mediaId.mediaId)
                putString(TimberMusicService.MEDIA_CALLER, mediaId.caller)
            }
            when (mediaId.type?.toInt()) {
                TimberMusicService.TYPE_ALL_SONGS -> return SongsFragment().apply {
                    arguments = args
                }
                TimberMusicService.TYPE_ALL_ALBUMS -> return AlbumsFragment().apply {
                    arguments = args
                }
                TimberMusicService.TYPE_ALL_PLAYLISTS -> return PlaylistFragment().apply {
                    arguments = args
                }
                TimberMusicService.TYPE_ALL_ARTISTS -> return ArtistFragment().apply {
                    arguments = args
                }
                TimberMusicService.TYPE_ALL_FOLDERS -> return FolderFragment().apply {
                    arguments = args
                }
                TimberMusicService.TYPE_ALL_GENRES -> return GenreFragment().apply {
                    arguments = args
                }
                TimberMusicService.TYPE_ALBUM -> return AlbumDetailFragment().apply {
                    arguments = args.apply {
                        putParcelable(Constants.ALBUM, mediaId.mediaItem)
                    }
                }
                TimberMusicService.TYPE_ARTIST -> return ArtistDetailFragment().apply {
                    arguments = args.apply {
                        putParcelable(Constants.ARTIST, mediaId.mediaItem)
                    }
                }
                TimberMusicService.TYPE_PLAYLIST -> return CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Playlist).apply {
                            putParcelable(Constants.CATEGORY_SONG_DATA,
                                    CategorySongData(name, songCount, TimberMusicService.TYPE_PLAYLIST, id))
                        }
                    }
                }
                TimberMusicService.TYPE_GENRE -> return CategorySongsFragment().apply {
                    arguments = args.apply {
                        (mediaId.mediaItem as Genre).apply {
                            putParcelable(Constants.CATEGORY_SONG_DATA,
                                    CategorySongData(name, songCount, TimberMusicService.TYPE_GENRE, id))
                        }
                    }
                }
                else -> return SongsFragment().apply {
                    arguments = args
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Always true, but lets lint know that as well.
        val context = activity ?: return
        mediaType = arguments?.getString(TimberMusicService.MEDIA_TYPE_ARG) ?: return
        mediaId = arguments?.getString(TimberMusicService.MEDIA_ID_ARG)
        caller = arguments?.getString(TimberMusicService.MEDIA_CALLER)

        mediaItemFragmentViewModel = ViewModelProviders
                .of(this, InjectorUtils.provideMediaItemFragmentViewModel(context, MediaID(mediaType, mediaId, caller)))
                .get(MediaItemFragmentViewModel::class.java)

        mainViewModel.customAction.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { action ->
                when (action) {
                    Constants.ACTION_SONG_DELETED -> mediaItemFragmentViewModel.reloadMediaItems()
                    Constants.ACTION_REMOVED_FROM_PLAYLIST -> mediaItemFragmentViewModel.reloadMediaItems()
                }
            }
        })
    }
}
