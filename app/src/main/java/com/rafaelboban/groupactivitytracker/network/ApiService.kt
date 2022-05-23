package com.rafaelboban.groupactivitytracker.network

import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.data.request.*
import com.rafaelboban.groupactivitytracker.data.response.DeleteMarkerResponse
import com.rafaelboban.groupactivitytracker.data.response.TokenResponse
import com.rafaelboban.groupactivitytracker.data.response.SimpleResponse
import com.rafaelboban.groupactivitytracker.data.response.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/register")
    suspend fun register(@Body request: RegisterRequest): SimpleResponse

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("/authenticate")
    suspend fun authenticate(): UserResponse

    @POST("/create-marker")
    suspend fun createMarker(@Body marker: MarkerRequest): Marker

    @GET("/markers")
    suspend fun getMarkers(): List<Marker>

    @POST("/delete-marker")
    suspend fun deleteMarker(@Body request: DeleteMarkerRequest): DeleteMarkerResponse

    @POST("/location")
    suspend fun saveLocation(@Body location: LocationRequest)

    @GET("/activities")
    suspend fun getActivities(): List<String>
}