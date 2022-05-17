package com.rafaelboban.groupactivitytracker.utils

sealed class Resource<out T> {
    class Success<T>(val data: T) : Resource<T>()
    class Error(val error: Throwable) : Resource<Nothing>()
}