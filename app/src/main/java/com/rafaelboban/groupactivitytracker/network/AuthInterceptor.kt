package com.rafaelboban.groupactivitytracker.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    private val ignoreAuthUrls = listOf("/register", "/login")

    private val token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath in ignoreAuthUrls) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}