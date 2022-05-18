package com.rafaelboban.groupactivitytracker.network

import com.rafaelboban.groupactivitytracker.data.request.LoginRequest
import com.rafaelboban.groupactivitytracker.data.request.RegisterRequest
import com.rafaelboban.groupactivitytracker.data.response.TokenResponse
import com.rafaelboban.groupactivitytracker.data.response.SimpleResponse
import com.rafaelboban.groupactivitytracker.data.response.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): SimpleResponse

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("/authenticate")
    suspend fun authenticate(): UserResponse

}