package com.rafaelboban.groupactivitytracker.utils

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> executeRequest(function: suspend () -> T): Resource<T> {
    return try {
        val result: T = function.invoke()
        if (result is Response<*> && !result.isSuccessful) {
            Resource.Error(HttpException(result))
        } else {
            Resource.Success(result)
        }
    } catch (e: Exception) {
        Resource.Error(e)
    }
}
