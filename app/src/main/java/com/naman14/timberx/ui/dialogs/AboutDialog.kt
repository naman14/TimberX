package com.naman14.timberx.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html

import com.afollestad.materialdialogs.MaterialDialog

import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import com.naman14.timberx.R

class AboutDialog : DialogFragment() {

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       return MaterialDialog(activity!!).show {
            title(text = "TimberX")
            message(text = Html.fromHtml(getString(R.string.about)))
            positiveButton(text = "Website") {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://namand.in")))
            }
           negativeButton(text = "Github") {
               startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/naman14/TimberX")))
           }
        }
    }
}
