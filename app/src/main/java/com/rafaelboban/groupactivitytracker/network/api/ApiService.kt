package com.rafaelboban.groupactivitytracker.network.api

import com.rafaelboban.groupactivitytracker.data.model.EventData
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.data.request.*
import com.rafaelboban.groupactivitytracker.data.response.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/api/register")
    suspend fun register(@Body request: RegisterRequest): SimpleResponse

    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("/api/authenticate")
    suspend fun authenticate(): UserResponse

    @POST("/api/create-marker")
    suspend fun createMarker(@Body marker: MarkerRequest): Marker

    @GET("/api/markers")
    suspend fun getMarkers(): List<Marker>

    @POST("/api/delete-marker")
    suspend fun deleteMarker(@Body request: DeleteMarkerRequest): DeleteMarkerResponse

    @GET("/api/events")
    suspend fun getEvents(): List<EventData>

    @POST("/api/create-event")
    suspend fun createEvent(@Body request: CreateEventRequest): CreateJoinEventResponse

    @POST("/api/join-event")
    suspend fun joinEvent(@Body request: JoinEventRequest): CreateJoinEventResponse

    @POST("/api/event-status")
    suspend fun eventStatus(@Body request: EventStatusRequest)
}