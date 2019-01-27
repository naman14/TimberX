package com.naman14.timberx.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import com.naman14.timberx.R
import com.naman14.timberx.ui.listeners.SortMenuListener

class SongSortMenu constructor(context: Context, attrs: AttributeSet): ImageView(context, attrs) {

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