package com.rafaelboban.groupactivitytracker.ui.event.adapter

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import com.rafaelboban.groupactivitytracker.databinding.MarkerInfoWindowBinding
import java.text.DecimalFormat

class MarkerInfoAdapter(val context: Context) : GoogleMap.InfoWindowAdapter {

    private val binding = MarkerInfoWindowBinding.inflate((context as Activity).layoutInflater, null, false)

    override fun getInfoContents(marker: Marker): View? {
        marker.snippet?.takeIf { it.isNotBlank() }?.let {
            renderViews(marker)
            return binding.root
        }
        return null
    }

    override fun getInfoWindow(marker: Marker): View? {
        marker.snippet?.takeIf { it.isNotBlank() }?.let {
            renderViews(marker)
            return binding.root
        }
        return null
    }

    private fun renderViews(marker: Marker) {
        marker.snippet?.takeIf { it.isNotBlank() }?.let {
            val markerData = Gson().fromJson(marker.snippet, MarkerData::class.java)
            binding.run {
                username.text = markerData.username
                distance.text = "${DecimalFormat("0.00").format(markerData.distance)} km"
                speed.text = "${DecimalFormat("0.00").format(markerData.speed)} km/h"
                direction.text = markerData.direction
            }
        }
    }

    data class MarkerData(
        val username: String,
        val distance: Double,
        val speed: Double,
        val direction: String,
    )
}