package com.naman14.timberx.util

import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.preference.PreferenceManager
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.naman14.timberx.R
import com.naman14.timberx.db.SongEntity
import com.naman14.timberx.repository.SongsRepository
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.models.Song

fun Song.toSongEntity(): SongEntity {
    return SongEntity(null, this.id)
}

fun ArrayList<Song>.toSongEntityList(): List<SongEntity> {

    val songEntityList = ArrayList<SongEntity>()
    for (song in this) {
        songEntityList.add(song.toSongEntity())
    }
    return songEntityList
}

fun List<SongEntity>.toSongIDs(context: Context): LongArray {
    val queue = LongArray(size)
    for (i in 0 until size) {
        queue[i] = this[i].id
    }
    return queue
}

fun List<Song>.toSongIDs(): LongArray {
    val queue = LongArray(size)
    for (i in 0 until size) {
        queue[i] = this[i].id
    }
    return queue
}

fun List<Song?>.toQueue(): List<MediaSessionCompat.QueueItem> {
    val queue = arrayListOf<MediaSessionCompat.QueueItem>()
    for (song in this) {
        queue.add(MediaSessionCompat.QueueItem(song?.toDescription(), song!!.id))
    }
    return queue
}

fun LongArray.toQueue(context: Context): List<MediaSessionCompat.QueueItem> {
    val songList = SongsRepository.getSongsForIDs(context, this)
    // the list returned above is sorted in default order, need to map it to same as the input array and preserve the original order
    songList.keepInOrder(this)?.let {
        return it.toQueue()
    } ?: return songList.toQueue()
}

fun List<Song>.keepInOrder(queue: LongArray): List<Song>? {
    if (isNotEmpty() && queue.isNotEmpty()) {
        val keepOrderList = Array<Song>(size, init = {Song()})
        forEach {
            keepOrderList[queue.indexOf(it.id)] =  it
        }
        return keepOrderList.asList()
    } else return null
}

fun LongArray.toSongEntityList(context: Context): List<SongEntity> {
    val songList = SongsRepository.getSongsForIDs(context, this)
    return songList.toSongEntityList()
}

fun List<MediaSessionCompat.QueueItem>.toIDList(): LongArray {
    val idList = LongArray(size)
    for (i in 0 until size) {
        idList[i] = this[i].queueId
    }
    return idList
}

fun Song.toDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(id.toString())
            .setSubtitle(artist)
            .setDescription(album)
            .setIconUri(Utils.getAlbumArtUri(albumId)).build()
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

fun <T> List<T>.moveElement(fromIndex: Int, toIndex: Int) = toMutableList().apply { add(toIndex, removeAt(fromIndex)) }.toList()

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
    supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.container, fragment)
                addToBackStack(null)
//                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
            .commit()
}

fun AppCompatActivity.addFragment(fragment: Fragment, tag: String? = null) {
    supportFragmentManager.beginTransaction()
            .apply {
                add(R.id.container, fragment, tag)
//                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                addToBackStack(null)
            }
            .commit()
}

fun defaultPrefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

fun statusbarColor(activity: Activity?, color: Int) {
    if (activity != null && Build.VERSION.SDK_INT >= 21) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = activity.resources.getColor(color)
    }
}

fun castSession(context: Context): CastSession? {
    return CastContext.getSharedInstance(context.applicationContext)?.sessionManager?.currentCastSession
}






