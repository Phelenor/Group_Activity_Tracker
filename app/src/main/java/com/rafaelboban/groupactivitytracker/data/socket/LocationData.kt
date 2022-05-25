package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_LOCATION_DATA

data class LocationData(
    val fromUserId: String,
    val fromUsername: String,
    val eventId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
) : BaseModel(TYPE_LOCATION_DATA)
