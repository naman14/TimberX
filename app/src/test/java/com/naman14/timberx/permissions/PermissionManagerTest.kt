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

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.content.pm.PackageManager.PERMISSION_GRANTED
import com.google.common.truth.Truth.assertThat
import com.naman14.timberx.TimberXApp
import com.naman14.timberx.permissions.RealPermissionsManager.Companion.REQUEST_CODE_STORAGE
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.Schedulers.trampoline
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE, sdk = [28], application = TimberXApp::class)
@RunWith(RobolectricTestRunner::class)
class PermissionManagerTest {

    private val app = mock<Application>()
    private val manager = RealPermissionsManager(app, trampoline())

    private val activity1 = mock<Activity>()
    private val activity2 = mock<Activity>()

    @After
    fun tearDown() = stopKoin()

    @Test
    fun attach() {
        manager.attach(activity1)
        assertThat(manager.activity).isSameAs(activity1)

        manager.attach(activity2)
        assertThat(manager.activity).isSameAs(activity2)
    }

    @Test
    fun hasStoragePermission() {
        whenever(app.checkPermission(eq(WRITE_EXTERNAL_STORAGE), any(), any()))
                .doReturn(PERMISSION_DENIED)
        assertThat(manager.hasStoragePermission()).isFalse()

        whenever(app.checkPermission(eq(WRITE_EXTERNAL_STORAGE), any(), any()))
                .doReturn(PERMISSION_GRANTED)
        assertThat(manager.hasStoragePermission()).isTrue()
    }

    @Test(expected = IllegalStateException::class)
    fun requestStoragePermission_notAttached() {
        whenever(app.checkPermission(eq(WRITE_EXTERNAL_STORAGE), any(), any()))
                .doReturn(PERMISSION_DENIED)
        manager.requestStoragePermission()
    }

    @Test
    fun requestStoragePermission_and_processResult() {
        val globalObservable = manager.onGrantResult().test()

        whenever(app.checkPermission(eq(WRITE_EXTERNAL_STORAGE), any(), any()))
                .doReturn(PERMISSION_DENIED)
        manager.attach(activity1)
        val testObs = manager.requestStoragePermission(waitForGranted = false).test()

        verify(activity1).requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
        testObs.assertNoValues().assertNotComplete().assertNoErrors()

        manager.processResult(REQUEST_CODE_STORAGE, arrayOf(WRITE_EXTERNAL_STORAGE), intArrayOf(PERMISSION_DENIED))
        manager.processResult(REQUEST_CODE_STORAGE, arrayOf(WRITE_EXTERNAL_STORAGE), intArrayOf(PERMISSION_GRANTED))

        // We only receive the first result because requestStoragePermission() returns a Single
        testObs.assertNoErrors()
                .assertComplete()
                .assertValues(GrantResult(WRITE_EXTERNAL_STORAGE, false))

        // However the global observer receives both emissions. This scenario will never actually happen
        // but this is logically how the Rx code works.
        globalObservable.assertNoErrors()
                .assertNotComplete()
                .assertValues(
                        GrantResult(WRITE_EXTERNAL_STORAGE, false),
                        GrantResult(WRITE_EXTERNAL_STORAGE, true)
                )
    }

    @Test
    fun requestStoragePermission_and_processResult_waitForGranted() {
        val globalObservable = manager.onGrantResult().test()

        whenever(app.checkPermission(eq(WRITE_EXTERNAL_STORAGE), any(), any()))
                .doReturn(PERMISSION_DENIED)
        manager.attach(activity1)
        val testObs = manager.requestStoragePermission(waitForGranted = true).test()

        verify(activity1).requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE)
        testObs.assertNoValues().assertNotComplete().assertNoErrors()

        manager.processResult(REQUEST_CODE_STORAGE, arrayOf(WRITE_EXTERNAL_STORAGE), intArrayOf(PERMISSION_DENIED))
        manager.processResult(REQUEST_CODE_STORAGE, arrayOf(WRITE_EXTERNAL_STORAGE), intArrayOf(PERMISSION_GRANTED))

        // We only receive the second result because requestStoragePermission() returns a Single
        // AND we specified waitForGranted = true.
        testObs.assertNoErrors()
                .assertComplete()
                .assertValues(GrantResult(WRITE_EXTERNAL_STORAGE, true))

        // However the global observer receives both emissions. This scenario will never actually happen
        // but this is logically how the Rx code works.
        globalObservable.assertNoErrors()
                .assertNotComplete()
                .assertValues(
                        GrantResult(WRITE_EXTERNAL_STORAGE, false),
                        GrantResult(WRITE_EXTERNAL_STORAGE, true)
                )
    }

    @Test
    fun requestStoragePermission_alreadyHavePermission() {
        whenever(app.checkPermission(eq(WRITE_EXTERNAL_STORAGE), any(), any()))
                .doReturn(PERMISSION_GRANTED)
        manager.attach(activity1)
        val testObs = manager.requestStoragePermission().test()

        verify(activity1, never()).requestPermissions(any(), any())
        testObs.assertValue(GrantResult(WRITE_EXTERNAL_STORAGE, true))
    }

    @Test
    fun detach() {
        manager.attach(activity1)
        assertThat(manager.activity).isSameAs(activity1)

        manager.attach(activity2)
        assertThat(manager.activity).isSameAs(activity2)

        manager.detach(activity1)
        assertThat(manager.activity).isSameAs(activity2)

        manager.detach(activity2)
        assertThat(manager.activity).isNull()
    }
}
