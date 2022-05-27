package com.rafaelboban.groupactivitytracker.data.model

data class LocationPoint(
    val userId: String,
    val eventId: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val id: String,
)