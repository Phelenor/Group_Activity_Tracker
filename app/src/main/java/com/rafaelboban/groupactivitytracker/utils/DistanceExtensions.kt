package com.rafaelboban.groupactivitytracker.utils

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.utils.sphericalDistance
import java.text.DecimalFormat

fun List<LatLng>.calculateDistance(): String {
    var total = 0.0
    for (i in 0 until lastIndex) {
        total += this[i].sphericalDistance(this[i + 1])
    }
    return DecimalFormat("0.00").format(total / 1000.0)
}

fun List<LatLng>.calculateDistance2(): String {
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
    return DecimalFormat("0.00").format(total / 1000.0)
}