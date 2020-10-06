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
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentNowPlayingBinding
import com.naman14.timberx.extensions.addFragment
import com.naman14.timberx.extensions.inflateWithBinding
import com.naman14.timberx.extensions.observe
import com.naman14.timberx.extensions.safeActivity
import com.naman14.timberx.models.QueueData
import com.naman14.timberx.network.models.ArtworkSize
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.bindings.setLastFmAlbumImage
import com.naman14.timberx.ui.dialogs.AboutDialog
import com.naman14.timberx.ui.fragments.base.BaseNowPlayingFragment
import com.naman14.timberx.util.AutoClearedValue
import org.koin.android.ext.android.inject

class NowPlayingFragmentCompose : BaseNowPlayingFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_now_playing_compose, container, false).apply {
            findViewById<ComposeView>(R.id.compose_view).setContent {
                NowPlayingScreen()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

    }

    private val RubikRegular = fontFamily(
            font(R.font.rubik_regular),
    )
    private val RubikMedium = fontFamily(
            font(R.font.rubik_medium),
    )

    private val TimberTypography = Typography(
            h6 = TextStyle(
                    fontFamily = RubikMedium,
                    fontSize = 20.sp
            ),
            body1 = TextStyle(
                    fontFamily = RubikRegular,
                    fontSize = 16.sp
            ),
            body2 = TextStyle(
                    fontFamily = RubikRegular,
                    fontSize = 14.sp
            ),

            )

    @Preview
    @Composable
    fun NowPlayingScreen() {
        MaterialTheme(typography = TimberTypography) {
            SongDetails()
        }
    }

    @Composable
    fun SongDetails() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                    text = "Eminem - Lose Yourself",
                    style = MaterialTheme.typography.h6
            )
            Text(
                    text = "Eminem",
                    style = MaterialTheme.typography.body1
            )
        }
    }
}
