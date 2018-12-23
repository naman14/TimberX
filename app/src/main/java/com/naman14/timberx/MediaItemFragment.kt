package com.naman14.timberx

import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.ui.albums.AlbumsFragment
import com.naman14.timberx.ui.artist.ArtistFragment
import com.naman14.timberx.ui.playlist.PlaylistFragment
import com.naman14.timberx.ui.songs.SongsFragment
import com.naman14.timberx.util.InjectorUtils

open class MediaItemFragment : NowPlayingFragment() {

    lateinit var mediaId: String
    lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    companion object {
        fun newInstance(mediaId: String): MediaItemFragment {

            when(mediaId.toInt()) {
                TimberMusicService.TYPE_SONG -> return SongsFragment().apply {
                    arguments = Bundle().apply {
                        putString(TimberMusicService.MEDIA_ID_ARG, mediaId)
                    }
                }
                TimberMusicService.TYPE_ALBUM -> return AlbumsFragment().apply {
                    arguments = Bundle().apply {
                        putString(TimberMusicService.MEDIA_ID_ARG, mediaId)
                    }
                }
                TimberMusicService.TYPE_PLAYLIST -> return PlaylistFragment().apply {
                    arguments = Bundle().apply {
                        putString(TimberMusicService.MEDIA_ID_ARG, mediaId)
                    }
                }
                TimberMusicService.TYPE_ARTIST -> return ArtistFragment().apply {
                    arguments = Bundle().apply {
                        putString(TimberMusicService.MEDIA_ID_ARG, mediaId)
                    }
                }
                else -> return SongsFragment().apply {
                    arguments = Bundle().apply {
                        putString(TimberMusicService.MEDIA_ID_ARG, mediaId)
                    }
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Always true, but lets lint know that as well.
        val context = activity ?: return
        mediaId = arguments?.getString(TimberMusicService.MEDIA_ID_ARG) ?: return

        mediaItemFragmentViewModel = ViewModelProviders
                .of(this, InjectorUtils.provideMediaItemFragmentViewModel(context, mediaId))
                .get(MediaItemFragmentViewModel::class.java)
    }
}
