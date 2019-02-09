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
package com.naman14.timberx.constants

import com.naman14.timberx.R

enum class AppThemes(val rawValue: String, val themeRes: Int) {
    LIGHT("light", R.style.AppTheme_Light),
    DARK("dark", R.style.AppTheme_Dark),
    BLACK("black", R.style.AppTheme_Black);

    companion object {
        fun fromString(raw: String): AppThemes {
            return AppThemes.values().single { it.rawValue == raw }
        }

        fun toString(value: AppThemes): String = value.rawValue
    }
}
