package com.rafaelboban.groupactivitytracker.utils

import com.rafaelboban.groupactivitytracker.data.model.Marker
import java.text.DecimalFormat

object KMLHelper {

    private val decimalFormat = DecimalFormat("0.00####")

    fun exportMarkersToKML(markers: List<Marker>): String {
        val kmlStringBuilder = StringBuilder()

        kmlStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        kmlStringBuilder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        kmlStringBuilder.append("<Document>\n")
        kmlStringBuilder.append("<name>group_activity_tracker.kml</name>\n")

        for (marker in markers) {
            val latitude = decimalFormat.format(marker.latitude)
            val longitude = decimalFormat.format(marker.longitude)
            kmlStringBuilder.append("<Placemark>\n")
            kmlStringBuilder.append("<name>${marker.title}</name>\n")
            marker.snippet?.let { kmlStringBuilder.append("<description>$it</description>\n") }
            kmlStringBuilder.append("<Point>\n")
            kmlStringBuilder.append("<coordinates>$longitude,$latitude,0</coordinates>\n")
            kmlStringBuilder.append("</Point>\n")
            kmlStringBuilder.append("</Placemark>\n")
        }

        kmlStringBuilder.append("</Document>\n")
        kmlStringBuilder.append("</kml>\n")
        return kmlStringBuilder.toString()
    }
}