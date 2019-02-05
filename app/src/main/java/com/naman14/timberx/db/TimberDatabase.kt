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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// TODO the database should be provided via DI not as a singleton here
@Database(entities = [QueueEntity::class, SongEntity::class], version = 1)
abstract class TimberDatabase : RoomDatabase() {

    abstract fun queueDao(): QueueDao

    companion object {
        private var INSTANCE: TimberDatabase? = null

        fun getInstance(context: Context): TimberDatabase? {
            if (INSTANCE == null) {
                synchronized(TimberDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            TimberDatabase::class.java, "queue.db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return INSTANCE
        }
    }
}
