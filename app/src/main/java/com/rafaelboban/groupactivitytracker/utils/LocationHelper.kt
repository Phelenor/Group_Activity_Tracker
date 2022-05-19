package com.rafaelboban.groupactivitytracker.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import java.text.DecimalFormat
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


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

    fun formatLatLng(latLng: LatLng): String {
        val decimalFormat = DecimalFormat("0.00####")
        val latitude = if (latLng.latitude < 0) {
            val temp = latLng.latitude.toString().removePrefix("-")
            "${decimalFormat.format(temp)}\\u00B0 S"
        } else {
            "${decimalFormat.format(latLng.latitude)}\u00B0 N"
        }

        val longitude = if (latLng.longitude < 0) {
            val temp = latLng.longitude.toString().removePrefix("-")
            "${decimalFormat.format(temp)}\\u00B0 W"
        } else {
            "${decimalFormat.format(latLng.longitude)}\u00B0 E"
        }

        return "$latitude, $longitude"
    }
}