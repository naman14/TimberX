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
package com.naman14.timberx.db

import android.app.Application
import androidx.room.Room
import org.koin.dsl.bind
import org.koin.dsl.module

val roomModule = module {

    single {
        Room.databaseBuilder(get<Application>(), TimberDatabase::class.java, "queue.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build()
    }

    factory { get<TimberDatabase>().queueDao() }

    factory {
        RealQueueHelper(get(), get())
    } bind QueueHelper::class
}
