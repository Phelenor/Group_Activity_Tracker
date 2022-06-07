package com.rafaelboban.groupactivitytracker.network.api

import android.content.SharedPreferences
import com.rafaelboban.groupactivitytracker.utils.Constants.PREFERENCE_USER_ID
import okhttp3.Interceptor
import okhttp3.Response

class UserInterceptor(private val preferences: SharedPreferences) : Interceptor {

    private val userId: String
        get() = preferences.getString(PREFERENCE_USER_ID, "")!!

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (userId.isNotBlank() && request.url.encodedPath == "/ws/event") {
            val url = chain.request().url.newBuilder()
                .addQueryParameter("user_id", userId)
                .build()
            val newRequest = chain.request().newBuilder()
                .url(url)
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(request)
    }
}