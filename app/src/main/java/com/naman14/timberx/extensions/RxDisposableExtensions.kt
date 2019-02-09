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

import android.view.View
import androidx.core.view.ViewCompat
import com.naman14.timberx.R
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.disposeOnDetach(view: View?) {
    if (view == null) return
    view.disposeOnDetach { this }
}

val View.isAttachedToWindowCompat: Boolean get() = ViewCompat.isAttachedToWindow(this)

fun View.disposeOnDetach(disposableFactory: () -> Disposable) {
    val attachedDisposables = ensureAttachedDisposable()
    if (isAttachedToWindowCompat) {
        // Run lambda synchronously, and after that, check again whether the view is still attached.
        val disposable = disposableFactory()
        if (isAttachedToWindowCompat) {
            // Since the view is still attached, register the disposable.
            attachedDisposables.disposable += disposable
        } else {
            // Since the view got detached, the disposable must be disposed immediately,
            disposable.dispose()
        }
    } else {
        // Defer lambda execution until the view is attached to window.
        attachedDisposables += disposableFactory
    }
}

private fun View.ensureAttachedDisposable(): AttachedDisposables {
    return getTag(R.id.tag_attached_disposables) as? AttachedDisposables
            ?: AttachedDisposables().apply {
                setTag(R.id.tag_attached_disposables, this)
                addOnAttachStateChangeListener(this)
            }
}

private class AttachedDisposables : View.OnAttachStateChangeListener {
    val disposable = CompositeDisposable()
    private val disposableFactories by lazy { mutableListOf<() -> Disposable>() }

    operator fun plusAssign(disposableFactory: () -> Disposable) {
        disposableFactories += disposableFactory
    }

    override fun onViewAttachedToWindow(v: View) {
        disposableFactories.apply {
            forEach { factory -> disposable += factory() }
            clear()
        }
    }

    override fun onViewDetachedFromWindow(v: View) = disposable.clear()
}

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}
