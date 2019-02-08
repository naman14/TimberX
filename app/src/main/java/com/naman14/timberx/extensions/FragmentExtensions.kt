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

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

inline val Fragment.safeActivity: FragmentActivity
    get() = activity ?: throw IllegalStateException("Fragment not attached")

fun <T> Fragment.argument(name: String): T {
    return arguments?.get(name) as? T ?: throw IllegalStateException("Argument $name not found.")
}

fun <T> Fragment.argumentOr(name: String, default: T): T {
    return arguments?.get(name) as? T ?: default
}

fun Fragment.argumentOrEmpty(name: String): String {
    return arguments?.get(name) as? String ?: ""
}
