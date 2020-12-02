package com.example.moveohealth.util

import androidx.lifecycle.LiveData


/**
 * A LiveData class that has `null` value.
 */
class AbsentLiveData<T : Any?> private constructor(
    private val isThread: Boolean
): LiveData<T>() {

    init {
        if (isThread) {
            postValue(null)
        } else {
            value = null
        }
    }

    companion object {
        fun <T> create(isOnBgThread: Boolean): LiveData<T> {
            return AbsentLiveData(isOnBgThread)
        }
    }
}