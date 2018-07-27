package com.naman14.timberx.util

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.TimberMusicService
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.vo.Song

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

fun getService(): TimberMusicService? {
    return TimberMusicService.mService
}

fun getSongUri(id: Long): Uri {
    return ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id)
}

fun RecyclerView.addOnItemClick(listener: RecyclerItemClickListener.OnClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, listener, null))
}

fun RecyclerView.addOnLongItemClick(listener: RecyclerItemClickListener.OnLongClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, null, listener))
}

fun RecyclerView.addOnItemClicks(onClick: RecyclerItemClickListener.OnClickListener, onLongClick: RecyclerItemClickListener.OnLongClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, onClick, onLongClick))
}

class doAsync(val handler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }
}

class doAsyncPost(val handler: () -> Unit, val postHandler: () -> Unit) : AsyncTask<Void, Void, Void>() {
    override fun doInBackground(vararg params: Void?): Void? {
        handler()
        return null
    }

    override fun onPostExecute(result: Void?) {
        postHandler()
    }
}

class doAsyncPostWithResult<T>(val handler: () -> T?, val postHandler: (bitmap: T?) -> Unit) : AsyncTask<Void, Void, T>() {
    override fun doInBackground(vararg params: Void?): T? {
        return handler()
    }

    override fun onPostExecute(result: T?) {
        postHandler(result)
    }
}

fun Fragment.navigateTo(fragment: Fragment) {
    activity?.let {
        val backStateName = fragment.javaClass.name

        val manager = activity!!.supportFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            val ft = manager.beginTransaction()
            ft.replace(R.id.container, fragment, backStateName)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
    }

}

fun AppCompatActivity.replaceFragment(fragment: Fragment) {
    val manager = supportFragmentManager
    val ft = manager.beginTransaction()
    ft.replace(R.id.container, fragment)
    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
    ft.commit()
}

fun defaultPrefs(context: Context): SharedPreferences
        = PreferenceManager.getDefaultSharedPreferences(context)

fun statusbarColor(activity: Activity?, color: Int) {
    if (activity != null && Build.VERSION.SDK_INT >= 21) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = activity.resources.getColor(color)
    }
}




