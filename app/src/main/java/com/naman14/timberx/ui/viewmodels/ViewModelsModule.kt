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

import com.naman14.timberx.models.MediaID
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val viewModelsModule = module {
    viewModel {
        MainViewModel(get(), get(), get(), get(), get(), get())
    }

    viewModel {
        SearchViewModel(get(), get(), get())
    }

    viewModel { (mediaId: MediaID) ->
        MediaItemFragmentViewModel(mediaId, get())
    }

    viewModel {
        NowPlayingViewModel(get())
    }
}
