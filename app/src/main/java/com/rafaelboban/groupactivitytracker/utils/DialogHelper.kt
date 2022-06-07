package com.rafaelboban.groupactivitytracker.utils

import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.TextWatcher
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.maps.android.ktx.addMarker
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.data.model.ParticipantData
import com.rafaelboban.groupactivitytracker.data.socket.Announcement
import com.rafaelboban.groupactivitytracker.data.socket.BaseModel
import com.rafaelboban.groupactivitytracker.data.socket.MarkerMessage
import com.rafaelboban.groupactivitytracker.databinding.MapTypeDialogBinding
import com.rafaelboban.groupactivitytracker.databinding.MarkerDialogBinding
import com.rafaelboban.groupactivitytracker.databinding.ParticipantsInfoDialogBinding
import com.rafaelboban.groupactivitytracker.ui.event.adapter.ParticipantInfoAdapter

object DialogHelper {

    fun showMarkerViewDialog(context: Context, marker: Marker, onRemove: () -> Unit) {
        MaterialAlertDialogBuilder(context).create().apply {
            setTitle(marker.title)
            marker.snippet.takeUnless { it.isNullOrEmpty() }?.let {
                setMessage(it)
            }
            setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.dismiss_lower)) { _, _ -> dismiss() }
            setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.delete)) { _, _ ->
                onRemove.invoke()
                dismiss()
            }
        }.show()
    }

    fun showParticipantInfoDialog(context: Context, participantAdapter: ParticipantInfoAdapter) {
        MaterialAlertDialogBuilder(context).create().apply {
            val dialogBinding = ParticipantsInfoDialogBinding.inflate(layoutInflater).apply {
                this.root.adapter = participantAdapter
                this.root.layoutManager = LinearLayoutManager(context)
            }

            setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.dismiss_lower)) { _, _ -> dismiss() }
            setView(dialogBinding.root)
        }.show()
    }

    fun showSelectMarkerTypeDialog(context: Context, googleMap: GoogleMap, setColorsCallback: (Int) -> Unit) {
        MaterialAlertDialogBuilder(context).create().apply {
            val dialogBinding = MapTypeDialogBinding.inflate(layoutInflater).apply {
                when (googleMap.mapType) {
                    GoogleMap.MAP_TYPE_NORMAL -> standard.isChecked = true
                    GoogleMap.MAP_TYPE_SATELLITE -> satellite.isChecked = true
                    GoogleMap.MAP_TYPE_HYBRID -> hybrid.isChecked = true
                }
            }
            setView(dialogBinding.root)
            setTitle(R.string.select_map_type)
            setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.dismiss_lower)) { _: DialogInterface?, _: Int -> dismiss() }
            setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.apply)) { _: DialogInterface?, _: Int ->
                when (findViewById<RadioButton>(dialogBinding.typesGroup.checkedRadioButtonId)) {
                    dialogBinding.standard -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                    dialogBinding.satellite -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    dialogBinding.hybrid -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                }
                setColorsCallback.invoke(googleMap.mapType)
                dismiss()
            }
        }.show()
    }

    fun showCreateMarkerDialog(
        context: Context,
        googleMap: GoogleMap,
        currentMarkerLatLng: LatLng,
        eventId: String,
        username: String,
        saveMarkerCallback: (String, Double, Double, String, String) -> Unit,
        sendBaseModelCallback: (BaseModel) -> Unit,
    ) {
        MaterialAlertDialogBuilder(context).create().apply {
            val dialogBinding = MarkerDialogBinding.inflate(layoutInflater).apply {

                etTitle.addTextChangedListener(object : TextWatcher {

                    override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        buttonSave.isEnabled = text.isNullOrEmpty().not()
                        buttonBroadcast.isEnabled = text.isNullOrEmpty().not()
                        buttonSaveBroadcast.isEnabled = text.isNullOrEmpty().not()
                    }

                    override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
                    override fun afterTextChanged(text: Editable?) = Unit

                })

                etTitle.setText("")
                etDescription.setText("")

                buttonSave.setOnClickListener {
                    val title = etTitle.text?.trim().toString()
                    val description = etDescription.text?.trim().toString()
                    googleMap.addMarker {
                        position(currentMarkerLatLng)
                        title(title)
                        snippet(description)
                    }
                    saveMarkerCallback.invoke(eventId, currentMarkerLatLng.latitude, currentMarkerLatLng.longitude, title, description)
                    dismiss()
                }

                buttonBroadcast.setOnClickListener {
                    val title = etTitle.text?.trim().toString()
                    val description = etDescription.text?.trim().toString()
                    val announcement = Announcement(
                        eventId,
                        "$username added a marker.",
                        System.currentTimeMillis(),
                        Announcement.TYPE_ADDED_MARKER
                    )

                    val markerMessage = MarkerMessage(
                        eventId,
                        currentMarkerLatLng.latitude,
                        currentMarkerLatLng.longitude,
                        title,
                        description
                    )
                    sendBaseModelCallback.invoke(announcement)
                    sendBaseModelCallback.invoke(markerMessage)
                    dismiss()
                }

                buttonSaveBroadcast.setOnClickListener {
                    val title = etTitle.text?.trim().toString()
                    val description = etDescription.text?.trim().toString()
                    googleMap.addMarker {
                        position(currentMarkerLatLng)
                        title(title)
                        snippet(description)
                    }
                    val announcement = Announcement(
                        eventId,
                        "$username added a marker.",
                        System.currentTimeMillis(),
                        Announcement.TYPE_ADDED_MARKER
                    )

                    val markerMessage = MarkerMessage(
                        eventId,
                        currentMarkerLatLng.latitude,
                        currentMarkerLatLng.longitude,
                        title,
                        description
                    )

                    sendBaseModelCallback.invoke(announcement)
                    sendBaseModelCallback.invoke(markerMessage)
                    saveMarkerCallback.invoke(eventId, currentMarkerLatLng.latitude, currentMarkerLatLng.longitude, title, description)
                    dismiss()
                }
            }
            setView(dialogBinding.root)
            setTitle(R.string.new_marker)
        }.show()
    }
}