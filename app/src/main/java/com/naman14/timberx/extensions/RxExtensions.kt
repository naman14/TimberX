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
@file:Suppress("unused")

package com.naman14.timberx.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io

fun <T> Observable<T>.ioToMain(): Observable<T> {
    return observeOn(mainThread())
            .subscribeOn(io())
}

class LifecycleAwareDisposable(
    private val disposable: Disposable
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun dispose() = disposable.dispose()
}

fun LifecycleOwner.ownRx(disposable: Disposable) {
    if (this.lifecycle.currentState == Lifecycle.State.DESTROYED) {
        disposable.dispose()
        return
    }
    this.lifecycle.addObserver(LifecycleAwareDisposable(disposable))
}

fun Disposable.attachLifecycle(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.ownRx(this)
}
