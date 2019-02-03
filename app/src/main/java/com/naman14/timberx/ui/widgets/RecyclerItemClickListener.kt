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

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(
    private val mRecycler: RecyclerView,
    private val clickListener: OnClickListener? = null,
    private val longClickListener: OnLongClickListener? = null
) : RecyclerView.OnChildAttachStateChangeListener {

    override fun onChildViewDetachedFromWindow(view: View) {
        view.setOnClickListener(null)
        view.setOnLongClickListener(null)
    }

    override fun onChildViewAttachedToWindow(view: View) {
        view.setOnClickListener { v -> setOnChildAttachedToWindow(v) }
    }

    private fun setOnChildAttachedToWindow(v: View?) {
        if (v != null) {
            val position = mRecycler.getChildLayoutPosition(v)
            if (position >= 0) {
                clickListener?.onItemClick(position, v)
                longClickListener?.onLongItemClick(position, v)
            }
        }
    }

    interface OnClickListener {
        fun onItemClick(position: Int, view: View)
    }

    interface OnLongClickListener {
        fun onLongItemClick(position: Int, view: View)
    }
}
