package com.naman14.timberx.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*


@Dao
interface QueueDao {

    @Query("SELECT * FROM queue_meta_data where id = 0")
    fun getQueueData(): LiveData<QueueEntity>

    @Query("SELECT current_id FROM queue_meta_data where id = 0")
    fun getQueueCurrentId(): LiveData<Int>

    @Query("SELECT current_id FROM queue_meta_data where id = 0")
    fun getQueueCurrentIdSync(): Int

    @Query("SELECT current_seek_pos FROM queue_meta_data where id = 0")
    fun getQueueCurrentSeekPos(): LiveData<Int>

    @Query("SELECT shuffle_mode FROM queue_meta_data where id = 0")
    fun getQueueShuffleMode(): LiveData<Int>

    @Query("SELECT repeat_mode FROM queue_meta_data where id = 0")
    fun getQueueRepeatMode(): LiveData<Int>

    @Query("UPDATE queue_meta_data SET repeat_mode  = :repeatMode where id = 0")
    fun setRepeatMode(repeatMode: Int)

    @Query("UPDATE queue_meta_data SET shuffle_mode  = :shuffleMode where id = 0")
    fun setShuffleMode(shuffleMode: Int)

    @Query("UPDATE queue_meta_data SET current_id  = :currentId where id = 0")
    fun setCurrentId(currentId: Int)

    @Query("UPDATE queue_meta_data SET current_seek_pos  = :currentSeekPos where id = 0")
    fun setCurrentSeekPos(currentSeekPos: Int)

    @Query("DELETE from queue_meta_data")
    fun clearQueueData()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(queue: QueueEntity)



    @Query("SELECT * FROM queue_songs")
    fun getQueueSongs(): LiveData<List<SongEntity>>

    @Query("SELECT * FROM queue_songs where id = :id")
    fun getQueueSongById(id: Int): SongEntity

    @Query("DELETE from queue_songs")
    fun clearQueueSongs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: SongEntity)

    @Delete
    fun delete(song: SongEntity)

}