package com.rafaelboban.groupactivitytracker.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.rafaelboban.groupactivitytracker.data.model.Marker
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

const val EXTERNAL = "external"

object KMLHelper {

    private val decimalFormat = DecimalFormat("0.00####")

    private fun convertMarkersToKMLString(markers: List<Marker>): String {
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

    fun exportMarkersToKML(context: Context, markers: List<Marker>) {
        val string = convertMarkersToKMLString(markers)

        val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, Constants.MARKERS_KML_FILENAME)
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            val extVolumeUri: Uri = MediaStore.Files.getContentUri(EXTERNAL)
            val fileUri = context.contentResolver.insert(extVolumeUri, values)
            context.contentResolver.openOutputStream(fileUri!!)
        } else {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
            val file = File(path, "markers.kml")
            FileOutputStream(file)
        }

        val bytes = string.toByteArray()
        outputStream?.write(bytes)
        outputStream?.close()
    }
}