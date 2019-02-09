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

import com.naman14.timberx.network.Outcome
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun <T> Observable<T>.subscribeForOutcome(onOutcome: (Outcome<T>) -> Unit): Disposable {
    return subscribe({ onOutcome(Outcome.success(it)) }, { onOutcome(processError(it)) })
}

private fun <T> processError(error: Throwable): Outcome<T> {
    return when (error) {
        is HttpException -> {
            val response = error.response()
            val body = response.errorBody()!!
            Outcome.apiError(getError(body, error))
        }
        is SocketTimeoutException, is IOException -> Outcome.failure(error)
        else -> Outcome.failure(error)
    }
}

private fun getError(
    responseBody: ResponseBody,
    throwable: Throwable
): Throwable {
    return try {
        val jsonObject = JSONObject(responseBody.string())
        Exception(jsonObject.getString("message"), throwable)
    } catch (e: Exception) {
        Exception(e.message ?: "$e")
    }
}
