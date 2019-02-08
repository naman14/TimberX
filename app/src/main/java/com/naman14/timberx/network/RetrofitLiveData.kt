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
package com.naman14.timberx.network

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

//Automatically enqueue retrofit calls when a observer is attached
class RetrofitLiveData<T>(private val call: Call<T>) : LiveData<Outcome<T>>(), Callback<T> {

    override fun onActive() {
        if (!call.isCanceled && !call.isExecuted) {
            postValue(Outcome.loading(true))
            call.enqueue(this)
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        postValue(Outcome.failure(t))
    }

    override fun onResponse(call: Call<T>, response: Response<T>) {
        val request = call.request()
        Timber.d("${request.method()} ${request.url()}: ${response.code()} ${response.message()}")

        val body = response.body()
        if (body != null) {
            postValue(Outcome.success(body))
        } else {
            postValue(Outcome.apiError(Throwable("response is null")))
        }
    }
}
