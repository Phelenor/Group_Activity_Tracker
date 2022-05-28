package com.rafaelboban.groupactivitytracker.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.rafaelboban.groupactivitytracker.data.model.Marker
import java.io.File
import java.io.FileOutputStream

const val EXTERNAL = "external"

object KMLHelper {

    private fun convertMarkersToKMLString(markers: List<Marker>): String {
        val kmlStringBuilder = StringBuilder()

        kmlStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        kmlStringBuilder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        kmlStringBuilder.append("<Document>\n")
        kmlStringBuilder.append("<name>group_activity_tracker.kml</name>\n")

        for (marker in markers) {
            kmlStringBuilder.append("<Placemark>\n")
            kmlStringBuilder.append("<name>${marker.title}</name>\n")
            marker.snippet?.let { kmlStringBuilder.append("<description>$it</description>\n") }
            kmlStringBuilder.append("<Point>\n")
            kmlStringBuilder.append("<coordinates>${marker.longitude},${marker.latitude},0</coordinates>\n")
            kmlStringBuilder.append("</Point>\n")
            kmlStringBuilder.append("</Placemark>\n")
        }

        kmlStringBuilder.append("</Document>\n")
        kmlStringBuilder.append("</kml>\n")
        return kmlStringBuilder.toString()
    }

    private fun convertPointsToKMLString(points: List<LatLng>): String {
        val kmlStringBuilder = StringBuilder()

        kmlStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        kmlStringBuilder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        kmlStringBuilder.append("<Document>\n")
        kmlStringBuilder.append("<name>group_activity_tracker.kml</name>\n")
        kmlStringBuilder.append("<Style id=\"globalStyles\">\n")
        kmlStringBuilder.append("<LineStyle id=\"line\">\n")
        kmlStringBuilder.append("<color>ffc934eb</color>\n")
        kmlStringBuilder.append("<width>4</width>\n")
        kmlStringBuilder.append("</LineStyle>\n")
        kmlStringBuilder.append("</Style>\n")
        kmlStringBuilder.append("<Placemark>\n")
        kmlStringBuilder.append("<styleUrl>#globalStyles</styleUrl>\n")
        kmlStringBuilder.append("<name>Route</name>\n")
        kmlStringBuilder.append("<LineString>\n")
        kmlStringBuilder.append("<coordinates>\n")

        for (latLng in points) {
            kmlStringBuilder.append("${latLng.longitude},${latLng.latitude},0\n")
        }

        val start = points.first()
        val end = points.last()

        kmlStringBuilder.append("</coordinates>\n")
        kmlStringBuilder.append("</LineString>\n")
        kmlStringBuilder.append("</Placemark>\n")

        kmlStringBuilder.append("<Placemark>\n")
        kmlStringBuilder.append("<name>Start</name>\n")
        kmlStringBuilder.append("<Point>\n")
        kmlStringBuilder.append("<coordinates>${start.longitude},${start.latitude},0</coordinates>\n")
        kmlStringBuilder.append("</Point>\n")
        kmlStringBuilder.append("</Placemark>\n")

        kmlStringBuilder.append("<Placemark>\n")
        kmlStringBuilder.append("<name>End</name>\n")
        kmlStringBuilder.append("<Point>\n")
        kmlStringBuilder.append("<coordinates>${end.longitude},${end.latitude},0</coordinates>\n")
        kmlStringBuilder.append("</Point>\n")
        kmlStringBuilder.append("</Placemark>\n")

        kmlStringBuilder.append("</Document>\n")
        kmlStringBuilder.append("</kml>\n")
        return kmlStringBuilder.toString()
    }

    fun exportMarkersToKML(context: Context, markers: List<Marker>, fileName: String) {
        val string = convertMarkersToKMLString(markers)
        writeStringToFile(context, string, fileName)
        Log.i("KML_EXPORT", string)
    }

    fun exportRouteToKML(context: Context, points: List<LatLng>, fileName: String) {
        val string = convertPointsToKMLString(points)
        writeStringToFile(context, string, fileName)
        Log.i("KML_EXPORT", string)
    }

    private fun writeStringToFile(context: Context, string: String, fileName: String) {
        val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.kml")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            val extVolumeUri: Uri = MediaStore.Files.getContentUri(EXTERNAL)
            val fileUri = context.contentResolver.insert(extVolumeUri, values)
            context.contentResolver.openOutputStream(fileUri!!)
        } else {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
            val file = File(path, "$fileName.kml")
            FileOutputStream(file)
        }

        val bytes = string.toByteArray()
        outputStream?.write(bytes)
        outputStream?.close()
    }
}