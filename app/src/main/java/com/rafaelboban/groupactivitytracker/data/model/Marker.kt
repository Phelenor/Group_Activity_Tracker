package com.rafaelboban.groupactivitytracker.data.model

import java.util.*

data class Marker(
    val title: String,
    val snippet: String?,
    val latitude: Double,
    val longitude: Double,
    val userId: String,
    val id: String = UUID.randomUUID().toString()
)