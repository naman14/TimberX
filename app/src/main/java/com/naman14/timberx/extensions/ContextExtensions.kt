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
package com.naman14.timberx.extensions

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

private var toast: Toast? = null

fun Context?.toast(message: String) {
    if (this == null) {
        return
    }
    toast?.cancel()
    toast = Toast.makeText(this, message, LENGTH_SHORT)
            .apply {
                show()
            }
}

fun Context?.toast(@StringRes message: Int) {
    if (this == null) {
        return
    }
    toast?.cancel()
    toast = Toast.makeText(this, message, LENGTH_SHORT)
            .apply {
                show()
            }
}

fun Fragment.drawable(@DrawableRes res: Int): Drawable? {
    val context = activity ?: return null
    return context.drawable(res)
}

fun Activity?.drawable(@DrawableRes res: Int): Drawable? {
    if (this == null) {
        return null
    }
    return ContextCompat.getDrawable(this, res)
}

@Suppress("UNCHECKED_CAST")
fun <T> Context.systemService(name: String): T {
    return getSystemService(name) as T
}
