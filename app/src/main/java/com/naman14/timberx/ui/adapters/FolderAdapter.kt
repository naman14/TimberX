package com.naman14.timberx.ui.adapters

import android.app.Activity
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Environment
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.naman14.timberx.repository.FoldersRepository
import com.naman14.timberx.models.Song

import java.io.File
import java.util.ArrayList

import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.repository.SongsRepository

class FolderAdapter(private val mContext: Activity) : RecyclerView.Adapter<FolderAdapter.ItemHolder>() {

    private var mFileSet: List<File>? = null
    private val mSongs: MutableList<Song>
    private var mRoot: File? = null
    private val mIcons: Array<Drawable>
    private var mBusy = false

    private val LAST_FOLDER = "last_folder"
    private val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
    private val root =  prefs.getString(LAST_FOLDER, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path)

    init {
        mIcons = arrayOf<Drawable>(
                ContextCompat.getDrawable(mContext, R.drawable.ic_folder_open_black_24dp)!!,
                ContextCompat.getDrawable(mContext, R.drawable.ic_folder_parent_dark)!!,
                ContextCompat.getDrawable(mContext, R.drawable.ic_file_music_dark)!!,
                ContextCompat.getDrawable(mContext, R.drawable.ic_timer_wait)!!)
        mSongs = ArrayList()
    }

    fun applyTheme(dark: Boolean) {
        val cf = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        for (d in mIcons) {
            if (dark) {
                d.colorFilter = cf
            } else {
                d.clearColorFilter()
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_folder_list, viewGroup, false)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = mFileSet!![i]
        val (id, albumId, artistId, title, artist, album, duration, trackNumber) = mSongs[i]
        itemHolder.title.text = localItem.name
        if (localItem.isDirectory) {
            itemHolder.albumArt.setImageDrawable(if ("build/generated/source/kaptKotlin" == localItem.name) mIcons[1] else mIcons[0])
        } else {
            //            ImageLoader.getInstance().displayImage(TimberUtils.getAlbumArtUri(song.albumId).toString(),
            //                    itemHolder.albumArt,
            //                    new DisplayImageOptions.Builder().
            //                            cacheInMemory(true).showImageOnFail(mIcons[2])
            //                            .resetViewBeforeLoading(true).build());
        }
    }

    override fun getItemCount(): Int {
        return mFileSet?.size ?: 0
    }


    fun goUpAsync(): Boolean {
        if (mRoot == null || mBusy) {
            return false
        }
        val parent = mRoot!!.parentFile
        return if (parent != null && parent.canRead()) {
            updateDataSetAsync(parent)
        } else {
            false
        }
    }

    fun updateDataSetAsync(newRoot: File): Boolean {
        if (mBusy) {
            return false
        }
        if ("build/generated/source/kaptKotlin" == newRoot.name) {
            goUpAsync()
            return false
        }
        mRoot = newRoot
        NavigateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mRoot)
        return true
    }

    fun getTextToShowInBubble(pos: Int): String {
        if (mBusy || mFileSet!!.size == 0)
            return ""
        try {
            val f = mFileSet!![pos]
            return if (f.isDirectory) {
                f.name[0].toString()
            } else {
                Character.toString(f.name[0])
            }
        } catch (e: Exception) {
            return ""
        }

    }

    private fun getSongsForFiles(files: List<File>) {
        mSongs.clear()
        for (file in files) {
            mSongs.add(SongsRepository.getSongFromPath(file.absolutePath, mContext))
        }
    }

    fun init() {
        updateDataSetAsync(File(root))
    }


    private inner class NavigateTask : AsyncTask<File, Void, List<File>>() {

        override fun onPreExecute() {
            super.onPreExecute()
            mBusy = true
        }

        override fun doInBackground(vararg params: File): List<File> {
            val files = FoldersRepository.getMediaFiles(params[0], true)
            getSongsForFiles(files)
            return files
        }

        override fun onPostExecute(files: List<File>) {
            super.onPostExecute(files)
            mFileSet = files
            notifyDataSetChanged()
            mBusy = false
            prefs.edit {
                putString(LAST_FOLDER, mRoot!!.path)
            }
        }
    }

    inner class ItemHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        var title: TextView
        var albumArt: ImageView

        init {
            this.title = view.findViewById(R.id.folder_title) as TextView
            this.albumArt = view.findViewById(R.id.album_art) as ImageView
            view.setOnClickListener(this)
        }

        override fun onClick(v: View) {
//            if (mBusy) {
//                return
//            }
//            val f = mFileSet!![adapterPosition]
//
//            if (f.isDirectory && updateDataSetAsync(f)) {
//                albumArt.setImageDrawable(mIcons[3])
//            } else if (f.isFile) {
//
//                val handler = Handler()
//                handler.postDelayed({
//                    var current = -1
//                    val songId = SongsRepository.getSongFromPath(mFileSet!![adapterPosition].absolutePath, mContext).id
//                    var count = 0
//                    for ((id) in mSongs) {
//                        if (id != -1) {
//                            count++
//                        }
//                    }
//                    val ret = LongArray(count)
//                    var j = 0
//                    for (i in 0 until itemCount) {
//                        if (mSongs[i].id != -1) {
//                            ret[j] = mSongs[i].id
//                            if (mSongs[i].id == songId) {
//                                current = j
//                            }
//                            j++
//                        }
//                    }
//                    playAll(mContext, ret, current, -1, TimberUtils.IdType.NA,
//                            false, mSongs[adapterPosition], false)
//                }, 100)


            }
        }


}