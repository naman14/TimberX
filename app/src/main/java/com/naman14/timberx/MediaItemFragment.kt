package com.naman14.timberx

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.util.InjectorUtils

open class MediaItemFragment : Fragment() {

    lateinit var mediaId: String
    lateinit var mainActivityViewModel: MainViewModel
    lateinit var mediaItemFragmentViewModel: MediaItemFragmentViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Always true, but lets lint know that as well.
        val context = activity ?: return
        mediaId = arguments?.getString(TimberMusicService.MEDIA_ID_ARG) ?: return

        mainActivityViewModel = ViewModelProviders
                .of(context, InjectorUtils.provideMainActivityViewModel(context))
                .get(MainViewModel::class.java)

        mediaItemFragmentViewModel = ViewModelProviders
                .of(this, InjectorUtils.provideMediaItemFragmentViewModel(context, mediaId))
                .get(MediaItemFragmentViewModel::class.java)
    }
}
