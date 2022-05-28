package com.rafaelboban.groupactivitytracker.ui.event_details

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.ActivityEventDetailsBinding
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.DisplayHelper
import com.rafaelboban.groupactivitytracker.utils.KMLHelper
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.DecimalFormat
import kotlin.math.roundToInt

@AndroidEntryPoint
class EventDetailsActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEventDetailsBinding.inflate(layoutInflater) }

    private val args by navArgs<EventDetailsActivityArgs>()

    private lateinit var googleMap: GoogleMap

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                exportRouteToKML()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        lifecycleScope.launchWhenCreated {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.event_details_map) as SupportMapFragment
            setupMap(mapFragment)
        }

        setupViews()
    }

    private fun setupViews() {
        binding.run {
            val timeFormat = DateFormat.getTimeFormat(this@EventDetailsActivity)
            val dateFormat = DateFormat.getDateFormat(this@EventDetailsActivity)
            val date = dateFormat.format(args.eventData.startTimestamp)
            val startTime = timeFormat.format(args.eventData.startTimestamp)
            val endTime = timeFormat.format(args.eventData.endTimestamp)
            val dateTimeString = "$date $startTime - $endTime"
            val timeInHours = (args.eventData.endTimestamp - args.eventData.startTimestamp) / 1000.0 / 60 / 60
            val avgSpeedKmh = DecimalFormat("0.00").format(args.eventData.distance / timeInHours)
            val participantsString = args.eventData.participants.joinToString()

            title.text = args.eventData.name
            dateTime.text = dateTimeString
            distance.text = if (args.eventData.distance < 1) {
                "${(args.eventData.distance * 1000).roundToInt()} m"
            } else {
                "${DecimalFormat("0.00").format(args.eventData.distance)} km"
            }
            avgSpeed.text = "$avgSpeedKmh km/h"
            participants.text = participantsString

            buttonExportKml.setOnClickListener {
                checkWriteToStoragePermissionAndExport()
            }
        }
    }

    private fun exportRouteToKML() {
        try {
            val latLngList = args.eventData.points.map { latLng -> LatLng(latLng.latitude, latLng.longitude) }
            KMLHelper.exportRouteToKML(this@EventDetailsActivity, latLngList, args.eventData.name)
            Snackbar.make(binding.root, getString(R.string.file_saved), Snackbar.LENGTH_LONG).show()
        } catch (e: IOException) {
            Snackbar.make(binding.root, getString(R.string.export_error), Snackbar.LENGTH_LONG).show()
        }
    }

    private suspend fun setupMap(mapFragment: SupportMapFragment) {
        googleMap = mapFragment.awaitMap()

        googleMap.uiSettings.run {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = false
            isScrollGesturesEnabled = false
            isCompassEnabled = false
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
        }

        drawRoute()
    }

    private fun drawRoute() {
        val latLngList = args.eventData.points.map { LatLng(it.latitude, it.longitude) }
        googleMap.addPolyline {
            width(DisplayHelper.convertDpToPx(this@EventDetailsActivity, Constants.POLYLINE_WIDTH_DP).toFloat())
            color(getColor(R.color.polyline_purple))
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(RoundCap())
            addAll(latLngList)
        }

        val padding = DisplayHelper.convertDpToPx(this, 32)
        val bounds = LatLngBounds.builder().run {
            latLngList.forEach { latLng -> include(latLng) }
            build()
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
    }

    private fun checkWriteToStoragePermissionAndExport() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> exportRouteToKML()
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                Snackbar.make(binding.root, getString(R.string.storage_permission_required), Snackbar.LENGTH_INDEFINITE).run {
                    setAction(getString(R.string.ok)) {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }.show()
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
}