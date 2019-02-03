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
package com.naman14.timberx.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import com.afollestad.materialdialogs.MaterialDialog

import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.naman14.timberx.R

class AboutDialog : DialogFragment() {

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(activity!!).show {
            title(text = "TimberX")
            message(text = getString(R.string.about))
            positiveButton(text = "Website") {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://namand.in")))
            }
            negativeButton(text = "Github") {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/naman14/TimberX")))
            }
        }
    }
}
