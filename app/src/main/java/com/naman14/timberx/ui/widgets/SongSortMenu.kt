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
package com.naman14.timberx.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.PopupMenu
import com.naman14.timberx.R
import com.naman14.timberx.ui.listeners.SortMenuListener

class SongSortMenu constructor(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

    private var sortMenuListener: SortMenuListener? = null

    init {
        setImageResource(R.drawable.ic_sort_black)
        setOnClickListener {
            val popupMenu = PopupMenu(context, this)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_sort_by_az -> sortMenuListener?.sortAZ()
                    R.id.menu_sort_by_za -> sortMenuListener?.sortZA()
                    R.id.menu_sort_by_year -> sortMenuListener?.sortYear()
                    R.id.menu_sort_by_duration -> sortMenuListener?.sortDuration()
                }
                true
            }
            popupMenu.inflate(R.menu.song_sort_by)
            popupMenu.show()
        }
    }

    fun setupMenu(listener: SortMenuListener?) {
        this.sortMenuListener = listener
    }
}
