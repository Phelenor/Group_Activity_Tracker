package com.rafaelboban.groupactivitytracker.data.model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class EventData(
    val id: String,
    val parentId: String,
    val name: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val distance: Double,
    val startTimestampParent: Long,
    val endTimestampParent: Long,
    val participants: List<String>,
) : Serializable {

    var points: List<LatLng> = emptyList()
}