package com.rafaelboban.groupactivitytracker.utils

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import java.io.IOException


object LocationHelper {

    fun getLatLngFromQuery(context: Context, query: String): LatLng? {
        return try {
            val address = Geocoder(context).getFromLocationName(query, 1)?.takeIf { it.size > 0 } ?: return null
            val location = address[0]
            LatLng(location.latitude, location.longitude)
        } catch (e: IOException) {
            null
        }
    }
}