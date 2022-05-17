package com.rafaelboban.groupactivitytracker.network

import com.rafaelboban.groupactivitytracker.data.request.LoginRequest
import com.rafaelboban.groupactivitytracker.data.request.RegisterRequest
import com.rafaelboban.groupactivitytracker.data.response.AuthResponse
import com.rafaelboban.groupactivitytracker.data.response.SimpleResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): SimpleResponse

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("/authenticate")
    suspend fun authenticate(@Header("Authorization") token: String)

}