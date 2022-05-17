package com.rafaelboban.groupactivitytracker.utils

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> safeResponse(func: suspend () -> T): Resource<T> {
    return try {
        val result: T = func.invoke()
        if (result is Response<*> && !result.isSuccessful) {
            Resource.Error(HttpException(result))
        } else {
            Resource.Success(result)
        }
    } catch (e: Exception) {
        Resource.Error(e)
    }
}

inline fun <T> Resource<T>.getDataOrElse(defaultValue: () -> T): T? = if (this is Resource.Success) {
    data
} else {
    defaultValue.invoke()
}

fun <T> Resource<T>.getDataOrNull(): T? = if (this is Resource.Success) {
    data
} else {
    null
}

fun <T> Resource<T>.isSuccess(): Boolean = this is Resource.Success