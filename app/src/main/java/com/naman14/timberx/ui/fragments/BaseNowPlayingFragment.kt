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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.naman14.timberx.R
import com.naman14.timberx.ui.activities.MainActivity
import com.naman14.timberx.ui.viewmodels.MainViewModel
import com.naman14.timberx.ui.viewmodels.NowPlayingViewModel
import com.naman14.timberx.util.InjectorUtils

open class BaseNowPlayingFragment : Fragment() {

    lateinit var nowPlayingViewModel: NowPlayingViewModel
    lateinit var mainViewModel: MainViewModel

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val context = activity ?: return

        mainViewModel = ViewModelProviders
                .of(context, InjectorUtils.provideMainActivityViewModel(context))
                .get(MainViewModel::class.java)

        nowPlayingViewModel = ViewModelProviders
                .of(this, InjectorUtils.provideNowPlayingViewModel(context))
                .get(NowPlayingViewModel::class.java)

        nowPlayingViewModel.currentData.observe(this, Observer { showHideBottomSheet() })
    }

    override fun onPause() {
        showHideBottomSheet()
        super.onPause()
    }

    private fun showHideBottomSheet() {
        val activity = activity as MainActivity
        nowPlayingViewModel.currentData.value?.let {
            if (it.title != null && it.title!!.isNotEmpty()) {
                if (activity.supportFragmentManager.findFragmentById(R.id.container) is NowPlayingFragment) {
                    activity.hideBottomSheet()
                } else activity.showBottomSheet()
            } else {
                activity.hideBottomSheet()
            }
        }
    }
}
