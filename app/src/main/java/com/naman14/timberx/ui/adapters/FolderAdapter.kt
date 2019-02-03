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
package com.naman14.timberx.ui.adapters

import android.app.Activity
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
import com.naman14.timberx.util.Utils
import com.naman14.timberx.util.toSongIDs
import com.squareup.picasso.Picasso

class FolderAdapter(private val mContext: Activity) : RecyclerView.Adapter<FolderAdapter.ItemHolder>() {

    private var mFileSet: List<File>? = null
    private val mSongs: MutableList<Song>
    private var mRoot: File? = null
    private val mIcons: Array<Drawable>
    private var mBusy = false

    private val LAST_FOLDER = "last_folder"
    private val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
    private val root = prefs.getString(LAST_FOLDER, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path)

    private lateinit var callback: (song: Song, queueIds: LongArray, title: String) -> Unit

    init {
        mIcons = arrayOf<Drawable>(
                ContextCompat.getDrawable(mContext, R.drawable.ic_folder_open_black_24dp)!!,
                ContextCompat.getDrawable(mContext, R.drawable.ic_folder_parent_dark)!!,
                ContextCompat.getDrawable(mContext, R.drawable.ic_file_music_dark)!!,
                ContextCompat.getDrawable(mContext, R.drawable.ic_timer_wait)!!)
        mSongs = ArrayList()
    }

    fun init(callback: (song: Song, queueIds: LongArray, title: String) -> Unit) {
        this.callback = callback
        updateDataSetAsync(File(root))
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_folder_list, viewGroup, false)
        return ItemHolder(v)
    }

    override fun onBindViewHolder(itemHolder: ItemHolder, i: Int) {
        val localItem = mFileSet!![i]
        val song = mSongs[i]
        itemHolder.title.text = localItem.name
        if (localItem.isDirectory) {
            itemHolder.albumArt.setImageDrawable(if (".." == localItem.name) mIcons[1] else mIcons[0])
        } else {
            Picasso.get().load(Utils.getAlbumArtUri(song.albumId)).error(R.drawable.ic_music_note).into(itemHolder.albumArt)
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
        if (".." == newRoot.name) {
            goUpAsync()
            return false
        }
        mRoot = newRoot
        NavigateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mRoot)
        return true
    }

    private fun getSongsForFiles(files: List<File>) {
        mSongs.clear()
        for (file in files) {
            mSongs.add(SongsRepository.getSongFromPath(file.absolutePath, mContext))
        }
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
            if (mBusy) {
                return
            }
            val f = mFileSet!![adapterPosition]

            if (f.isDirectory && updateDataSetAsync(f)) {
                albumArt.setImageDrawable(mIcons[3])
            } else if (f.isFile) {
                val song = SongsRepository.getSongFromPath(mFileSet!![adapterPosition].absolutePath, mContext)
                callback(song, mSongs.subList(1, mSongs.size).toSongIDs(), mRoot?.name ?: "Folder")
            }
        }
    }
}
