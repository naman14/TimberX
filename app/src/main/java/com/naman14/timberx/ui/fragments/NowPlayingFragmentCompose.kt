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
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.FirstBaseline
import androidx.compose.foundation.text.LastBaseline
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Slider
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.HorizontalAlignmentLine
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.ui.tooling.preview.Preview
import com.naman14.timberx.DarkColors
import com.naman14.timberx.LightColors
import com.naman14.timberx.R
import com.naman14.timberx.TimberTypography
import com.naman14.timberx.databinding.FragmentNowPlayingBinding
import com.naman14.timberx.extensions.addFragment
import com.naman14.timberx.extensions.inflateWithBinding
import com.naman14.timberx.extensions.observe
import com.naman14.timberx.extensions.safeActivity
import com.naman14.timberx.models.MediaData
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
                NowPlayingScreen(nowPlayingViewModel.currentData, nowPlayingViewModel.queueData)
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

    }

    @Composable
    fun NowPlayingScreen(currentSongData: LiveData<MediaData>, queueData: LiveData<QueueData>) {
        MaterialTheme(
                typography = TimberTypography,
                colors = LightColors) {
            Column {
                SongDetails(currentSongData)
                SongProgress()
                MusicControls()
                UpNextSongDetails()
            }
        }
    }

    @Composable
    fun SongDetails(currentSongLiveData: LiveData<MediaData>) {

        val currentSongData by currentSongLiveData.observeAsState(initial = MediaData())

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .padding(horizontal = 70.dp)
                        .fillMaxWidth()
        )
        {
            Text(
                    text = currentSongData.title ?: "",
                    style = MaterialTheme.typography.h6
            )
            Text(
                    text = currentSongData.artist ?: "",
                    style = MaterialTheme.typography.body1
            )
        }
    }

    @Composable
    fun SongProgress() {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .padding(horizontal = 70.dp)
                        .fillMaxWidth()
        ) {
            val sliderValue = remember { mutableStateOf(30f) }
            Slider(
                    value = sliderValue.value,
                    valueRange = 0f..100f,
                    onValueChange = {
                        sliderValue.value = it
                    }
            )
            Row() {
                Text("0:50", modifier = Modifier.weight(1f), style = MaterialTheme.typography.body2)
                Text(text = "5:18", style = MaterialTheme.typography.body2)
            }
        }
    }

    @Composable
    fun MusicControls() {
        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                        .padding(horizontal = 80.dp)
                        .fillMaxWidth()
        ) {
            Row() {
                Image(vectorResource(id = R.drawable.ic_previous_outline),
                        modifier = Modifier.weight(1f).size(35.dp))
                Image(vectorResource(id = R.drawable.ic_play_outline),
                        modifier = Modifier.weight(1f).size(35.dp))
                Image(vectorResource(id = R.drawable.ic_skip_outline),
                        modifier = Modifier.weight(1f).size(35.dp))
            }
        }
    }
    
    @Composable
    fun UpNextSongDetails() {
        Column(
                modifier = Modifier.border(
                        1.dp,
                        colorResource(id = R.color.colorInvertedAlpha),
                        RoundedCornerShape(8.dp)
                )
        ) {
            Text("Up Next")
            Row() {
                Row() {
                    Column() {
                        Text("Eminem")
                        Text("Lose yourself")
                    }
                }
            }
        }
    }
}
