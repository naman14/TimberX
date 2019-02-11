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
@file:Suppress("MemberVisibilityCanBePrivate")

package com.naman14.timberx.ui.activities.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.naman14.timberx.permissions.PermissionsManager
import org.koin.android.ext.android.inject

/**
 * Automatically attaches and detaches the activity from [PermissionsManager]. Also automatically
 * handles permission results by pushing them back into the manager.
 */
abstract class PermissionsActivity : AppCompatActivity() {
    protected val permissionsManager by inject<PermissionsManager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsManager.attach(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionsManager.processResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        permissionsManager.detach(this)
        super.onDestroy()
    }
}
