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
package com.naman14.timberx.network.models

import com.google.gson.annotations.SerializedName

enum class ArtworkSize(val apiValue: String) {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large"),
    EXTRA_LARGE("extralarge"),
    MEGA("mega")
}

data class LastfmAlbum(@SerializedName("image") val artwork: List<Artwork>)

fun List<Artwork>.ofSize(size: ArtworkSize): Artwork {
    val result = firstOrNull { it.size == size.apiValue } ?: last()
    return if (size == ArtworkSize.MEGA) {
        result.copy(url = result.url.replace("300x300", "700x700"))
    } else {
        result
    }
}
