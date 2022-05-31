package com.rafaelboban.groupactivitytracker.network.api

import com.rafaelboban.groupactivitytracker.data.model.EventData
import com.rafaelboban.groupactivitytracker.data.model.LocationPoint
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.data.request.*
import com.rafaelboban.groupactivitytracker.data.response.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("/api/register")
    suspend fun register(@Body request: RegisterRequest): SimpleResponse

    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("/api/authenticate")
    suspend fun authenticate(): UserResponse

    @GET("/api/events")
    suspend fun getEvents(): List<EventData>

    @POST("/api/create-event")
    suspend fun createEvent(@Body request: CreateEventRequest): CreateJoinEventResponse

    @POST("/api/join-event")
    suspend fun joinEvent(@Body request: JoinEventRequest): CreateJoinEventResponse

    @GET("/api/event-status/{eventId}")
    suspend fun eventStatus(@Path("eventId") eventId: String)

    @GET("/api/points/{eventId}")
    suspend fun getPoints(@Path("eventId") eventId: String): List<LocationPoint>

    @POST("/api/save-marker")
    suspend fun saveMarker(@Body marker: MarkerRequest): Marker
}