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
@file:Suppress("UNCHECKED_CAST")

package com.naman14.timberx.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : View> LayoutInflater.inflateTo(
    @LayoutRes layoutRes: Int,
    parent: ViewGroup? = null,
    attachToRoot: Boolean = false
): T = inflate(layoutRes, parent, attachToRoot) as T

fun <T : ViewDataBinding> LayoutInflater.inflateWithBinding(
    @LayoutRes layoutRes: Int,
    parent: ViewGroup?,
    attachToRoot: Boolean = false
): T {
    return DataBindingUtil.inflate(this, layoutRes, parent, attachToRoot) as T
}
