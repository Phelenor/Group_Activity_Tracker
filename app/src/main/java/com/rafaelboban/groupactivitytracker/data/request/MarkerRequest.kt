package com.rafaelboban.groupactivitytracker.data.request

data class MarkerRequest(
    val eventId: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String?,
)