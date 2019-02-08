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
package com.naman14.timberx.network.conversion

import com.naman14.timberx.network.RetrofitLiveData
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

/**
 * A Retrofit adapter that converts the Call into a LiveData of ApiResponse.
 * @param <R>
</R> */
class LiveDataCallAdapter<R>(private val responseType: Type) :
    CallAdapter<R, RetrofitLiveData<R>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>): RetrofitLiveData<R> {
        return RetrofitLiveData(call)
    }
}
