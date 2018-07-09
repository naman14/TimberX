package com.naman14.timberx.util

import android.content.ContentUris
import android.net.Uri
import android.os.AsyncTask
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.vo.Song

fun RecyclerView.addOnItemClick(listener: RecyclerItemClickListener.OnClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, listener, null))
}

fun RecyclerView.addOnLongItemClick(listener: RecyclerItemClickListener.OnLongClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, null, listener))
}

fun RecyclerView.addOnItemClicks(onClick: RecyclerItemClickListener.OnClickListener, onLongClick: RecyclerItemClickListener.OnLongClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, onClick, onLongClick))
}

fun Song.toSongEntity(): SongEntity {
    return SongEntity(null, this.id, this.albumId, this.artistId, this.title, this.artist, this.album, this.duration, this.trackNumber)
}

fun SongEntity.toSong(): Song {
    return Song(this.id, this.albumId, this.artistId, this.title, this.artist, this.album, this.duration, this.trackNumber)
}

fun ArrayList<Song>.toSongEntityList(): List<SongEntity> {

    val songEntityList = ArrayList<SongEntity>()
    for (song in this) {
        songEntityList.add(song.toSongEntity())
    }
    return songEntityList
}

fun List<SongEntity>.toSongList(): ArrayList<Song> {

    val songList = ArrayList<Song>()
    for (songEntity in this) {
        songList.add(songEntity.toSong())
    }
    return songList
}

class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

fun getService(): TimberMusicService? {
    return TimberMusicService.mService
}

fun getSongUri(id: Long): Uri {
    return ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id)
}


