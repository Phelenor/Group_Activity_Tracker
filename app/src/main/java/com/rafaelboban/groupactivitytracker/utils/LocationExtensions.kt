package com.rafaelboban.groupactivitytracker.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.google.maps.android.ktx.utils.sphericalDistance
import com.google.maps.android.ktx.utils.sphericalHeading
import com.rafaelboban.groupactivitytracker.data.socket.LocationData
import kotlin.math.*


enum class LocationDirection(val value: String) {
    UNKNOWN(""),
    NORTH("North"),
    NORTH_EAST("North East"),
    EAST("East"),
    SOUTH_EAST("South East"),
    SOUTH("South"),
    SOUTH_WEST("South West"),
    WEST("West"),
    NORTH_WEST("North West")
}

fun List<LocationData>.calculateDistance(): Double {
    var total = 0.0
    for (i in 0 until lastIndex) {
        val startLatLng = LatLng(this[i].latitude, this[i].longitude)
        val endLatLng = LatLng(this[i + 1].latitude, this[i + 1].longitude)
        total += startLatLng.sphericalDistance(endLatLng)
    }
    return total / 1000.0
}

fun List<LocationData>.calculateDistance2(): Double {
    var total = 0.0
    for (i in 0 until lastIndex) {
        val a = this[i]
        val b = this[i + 1]
        val results = FloatArray(1)
        Location.distanceBetween(
            a.latitude, a.longitude,
            b.latitude, b.longitude,
            results
        )
        total += results[0]
    }
    return total / 1000.0
}

fun List<LocationData>.calculateAverageSpeed(): Double {
    var total = 0.0
    for (i in 0 until lastIndex) {
        val a = this[i]
        val b = this[i + 1]
        val results = FloatArray(1)
        Location.distanceBetween(
            a.latitude, a.longitude,
            b.latitude, b.longitude,
            results
        )
        total += results[0]
    }
    return total / 1000.0
}

fun List<LocationData>.calculateSpeed(): Double {
    val start = this.first()
    val end = this.last()

    val startLatLng = LatLng(start.latitude, start.longitude)
    val endLatLng = LatLng(end.latitude, end.longitude)

    val distance = startLatLng.sphericalDistance(endLatLng) / 1000
    val dTime = (end.timestamp - start.timestamp) / (1000.0 * 60 * 60)
    return distance / dTime
}

fun List<LocationData>.getDirection(): LocationDirection {
    val start = this.first()
    val end = this.last()

    val startLatLng = LatLng(start.latitude, start.longitude)
    val endLatLng = LatLng(end.latitude, end.longitude)

    val delta = 22.5
    val heading = startLatLng.sphericalHeading(endLatLng)
    var direction = LocationDirection.UNKNOWN

    if (heading >= 0 && heading < delta || heading < 0 && heading >= -delta) {
        direction = LocationDirection.NORTH
    } else if (heading >= delta && heading < 90 - delta) {
        direction = LocationDirection.NORTH_EAST
    } else if (heading >= 90 - delta && heading < 90 + delta) {
        direction = LocationDirection.EAST
    } else if (heading >= 90 + delta && heading < 180 - delta) {
        direction = LocationDirection.SOUTH_EAST
    } else if (heading >= 180 - delta || heading <= -180 + delta) {
        direction = LocationDirection.SOUTH
    } else if (heading >= -180 + delta && heading < -90 - delta) {
        direction = LocationDirection.SOUTH_WEST
    } else if (heading >= -90 - delta && heading < -90 + delta) {
        direction = LocationDirection.WEST
    } else if (heading >= -90 + delta && heading < -delta) {
        direction = LocationDirection.NORTH_WEST
    }

    return direction
}
