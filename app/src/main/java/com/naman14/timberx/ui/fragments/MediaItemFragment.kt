package com.naman14.timberx.ui.fragments

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.ui.viewmodels.MediaItemFragmentViewModel
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.models.CategorySongData
import com.naman14.timberx.models.Genre
import com.naman14.timberx.models.Playlist
import com.naman14.timberx.util.Constants
import com.naman14.timberx.util.InjectorUtils
import com.naman14.timberx.models.MediaID

open class MediaItemFragment : BaseNowPlayingFragment() {

    lateinit var mediaType: String
    lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    private var mediaId: String? = null

    companion object {
        fun newInstance(mediaId: MediaID): MediaItemFragment {

            val args = Bundle().apply {
                putString(TimberMusicService.MEDIA_TYPE_ARG, mediaId.type)
                putString(TimberMusicService.MEDIA_ID_ARG, mediaId.mediaId)
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

        mediaItemFragmentViewModel = ViewModelProviders
                .of(this, InjectorUtils.provideMediaItemFragmentViewModel(context, MediaID(mediaType, mediaId)))
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
