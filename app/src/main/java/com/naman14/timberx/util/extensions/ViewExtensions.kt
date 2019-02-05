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
package com.naman14.timberx.util.extensions

import android.app.Activity
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.naman14.timberx.R
import com.naman14.timberx.ui.widgets.RecyclerItemClickListener
import com.naman14.timberx.ui.widgets.RecyclerViewItemClickListener

fun RecyclerView.addOnItemClick(listener: RecyclerViewItemClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, listener, null))
}

fun RecyclerView.addOnLongItemClick(listener: RecyclerViewItemClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, null, listener))
}

fun RecyclerView.addOnItemClicks(onClick: RecyclerViewItemClickListener, onLongClick: RecyclerViewItemClickListener) {
    this.addOnChildAttachStateChangeListener(RecyclerItemClickListener(this, onClick, onLongClick))
}

fun Fragment.navigateTo(fragment: Fragment) {
    activity?.let {
        val backStateName = fragment.javaClass.name

        val manager = activity!!.supportFragmentManager
        val fragmentPopped = manager.popBackStackImmediate(backStateName, 0)

        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            val ft = manager.beginTransaction()
            ft.replace(R.id.container, fragment, backStateName)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            ft.addToBackStack(backStateName)
            ft.commit()
        }
    }
}

fun AppCompatActivity.replaceFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.container, fragment)
                addToBackStack(null)
            }
            .commit()
}

fun Activity?.addFragment(fragment: Fragment, tag: String? = null) {
    val compatActivity = this as? AppCompatActivity ?: return
    compatActivity.supportFragmentManager.beginTransaction()
            .apply {
                add(R.id.container, fragment, tag)
                addToBackStack(null)
            }
            .commit()
}

fun Activity?.setStatusBarColor(color: Int) {
    if (this != null && Build.VERSION.SDK_INT >= 21) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = resources.getColor(color)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : View> ViewGroup.inflate(@LayoutRes layout: Int): T {
    return LayoutInflater.from(context).inflate(layout, this, false) as T
}

fun <T : ViewDataBinding> ViewGroup.inflateWithBinding(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): T {
    val layoutInflater = LayoutInflater.from(context)
    return DataBindingUtil.inflate(layoutInflater, layoutRes, this, attachToRoot) as T
}
