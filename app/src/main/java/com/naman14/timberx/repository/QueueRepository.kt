package com.naman14.timberx.repository

import android.content.Context
import com.naman14.timberx.vo.Song

object QueueRepository {

    fun loadQueueSongs(context: Context): ArrayList<Song> {

        return SongsRepository.getSongsForCursor(SongsRepository.makeSongCursor(context, null, null))
    }
}