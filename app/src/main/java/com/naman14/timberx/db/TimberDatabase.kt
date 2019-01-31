package com.naman14.timberx.db

import android.content.Context
import androidx.room.*

@Database(entities = arrayOf(QueueEntity::class, SongEntity::class), version = 1)
abstract class TimberDatabase: RoomDatabase() {

    abstract fun queueDao(): QueueDao

    companion object {
        private var INSTANCE: TimberDatabase? = null

        fun getInstance(context: Context): TimberDatabase? {
            if (INSTANCE == null) {
                synchronized(TimberDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
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