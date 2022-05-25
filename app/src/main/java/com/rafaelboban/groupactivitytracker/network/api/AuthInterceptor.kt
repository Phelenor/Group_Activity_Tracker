package com.rafaelboban.groupactivitytracker.network.api

import android.content.SharedPreferences
import android.util.Log
import com.rafaelboban.groupactivitytracker.utils.Constants.PREFERENCE_JWT_TOKEN
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val preferences: SharedPreferences) : Interceptor {

    private val token: String
        get() = preferences.getString(PREFERENCE_JWT_TOKEN, "")!!

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath in setOf("/api/register", "/api/login", "/ws/event")) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}