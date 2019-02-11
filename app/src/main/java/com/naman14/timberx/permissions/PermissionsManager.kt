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
package com.naman14.timberx.permissions

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naman14.timberx.extensions.asString
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.Single.just
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

data class GrantResult(
    val permission: String,
    val granted: Boolean
)

/**
 * Helps us manage, check, and dispatch permission requests without much boiler plate in our Activities
 * or views.
 */
interface PermissionsManager {

    fun onGrantResult(): Observable<GrantResult>

    fun attach(activity: Activity)

    fun hasStoragePermission(): Boolean

    fun requestStoragePermission(waitForGranted: Boolean = false): Single<GrantResult>

    fun processResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

    fun detach(activity: Activity)
}

class RealPermissionsManager(
    private val context: Application,
    private val mainScheduler: Scheduler
) : PermissionsManager {

    companion object {
        @VisibleForTesting(otherwise = PRIVATE)
        const val REQUEST_CODE_STORAGE = 69
    }

    @VisibleForTesting(otherwise = PRIVATE)
    var activity: Activity? = null
    private val relay = PublishSubject.create<GrantResult>()

    override fun onGrantResult(): Observable<GrantResult> = relay.share().observeOn(mainScheduler)

    override fun attach(activity: Activity) {
        Timber.d("attach(): $activity")
        this.activity = activity
    }

    override fun hasStoragePermission() = hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun requestStoragePermission(waitForGranted: Boolean) =
            requestPermission(REQUEST_CODE_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, waitForGranted)

    override fun processResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Timber.d("processResult(): requestCode= %d, permissions: %s, grantResults: %s",
                requestCode, permissions.asString(), grantResults.asString())
        for ((index, permission) in permissions.withIndex()) {
            val granted = grantResults[index] == PERMISSION_GRANTED
            val result = GrantResult(permission, granted)
            Timber.d("Permission grant result: %s", result)
            relay.onNext(result)
        }
    }

    override fun detach(activity: Activity) {
        // === is referential equality - returns true if they are the same instance
        if (this.activity === activity) {
            Timber.d("detach(): $activity")
            this.activity = null
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PERMISSION_GRANTED
    }

    private fun requestPermission(code: Int, permission: String, waitForGranted: Boolean): Single<GrantResult> {
        Timber.d("Requesting permission: %s", permission)
        if (hasPermission(permission)) {
            Timber.d("Already have this permission!")
            return just(GrantResult(permission, true).also {
                relay.onNext(it)
            })
        }

        val attachedTo = activity ?: throw IllegalStateException("Not attached")
        ActivityCompat.requestPermissions(attachedTo, arrayOf(permission), code)
        return onGrantResult()
                .filter { it.permission == permission }
                .filter {
                    if (waitForGranted) {
                        // If we are waiting for granted, only allow emission if granted is true
                        it.granted
                    } else {
                        // Else continue
                        true
                    }
                }
                .take(1)
                .singleOrError()
    }
}
