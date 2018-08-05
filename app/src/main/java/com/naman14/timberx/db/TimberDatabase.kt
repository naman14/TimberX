package com.naman14.timberx.db

import android.content.Context
import androidx.room.*
import com.naman14.timberx.vo.Song

@Database(entities = arrayOf(QueueEntity::class, SongEntity::class), version = 8)
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

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}