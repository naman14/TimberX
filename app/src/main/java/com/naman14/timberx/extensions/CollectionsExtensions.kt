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

fun <T> List<T>?.moveElement(fromIndex: Int, toIndex: Int): List<T> {
    if (this == null) {
        return emptyList()
    }
    return toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
}

fun <T> List<T>.equalsBy(other: List<T>, by: (left: T, right: T) -> Boolean): Boolean {
    if (this.size != other.size) {
        return false
    }
    for ((index, item) in withIndex()) {
        val otherItem = other[index]
        val itemsEqual = by(item, otherItem)
        if (!itemsEqual) {
            return false
        }
    }
    return true
}

fun IntArray.asString(): String {
    return joinToString(separator = ", ", prefix = "[", postfix = "]")
}

fun LongArray.asString(): String {
    return joinToString(separator = ", ", prefix = "[", postfix = "]")
}

fun <T> Array<T>.asString(): String {
    return joinToString(separator = ", ", prefix = "[", postfix = "]")
}
