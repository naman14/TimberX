package com.naman14.timberx.lastfm

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//Automatically enqueue retrofit calls when a observer is attached
class RetrofitLiveData<T>(private val call: Call<T>) : LiveData<Outcome<T>>(), Callback<T> {

    override fun onActive() {
        if (!call.isCanceled && !call.isExecuted) {
            postValue(Outcome.loading(true))
            call.enqueue(this)
        }
    }

    override fun onFailure(call: Call<T>?, t: Throwable?) {
        postValue(Outcome.failure(t!!))
    }

    override fun onResponse(call: Call<T>?, response: Response<T>?) {
        if (response?.body() != null) {
            postValue(Outcome.success(response.body()!!))
        } else {
            postValue(Outcome.apiError(Throwable("response is null")))
        }
    }

    fun cancel() = if(!call.isCanceled) call.cancel() else Unit

    //helper method to directly enqueue call instead of attaching observer to livedata
    fun makeCall() = call.enqueue(this)
} 