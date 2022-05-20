package com.rafaelboban.groupactivitytracker.ui.event

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.ActivityEventBinding
import com.rafaelboban.groupactivitytracker.services.TrackerService
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.DisplayHelper
import kotlinx.coroutines.launch

class EventActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEventBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<EventViewModel>()

    private lateinit var googleMap: GoogleMap
    private lateinit var locationClient: FusedLocationProviderClient

    private var locationList = emptyList<LatLng>()
    private val polylines = mutableListOf<Polyline>()
    private var firstLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launchWhenCreated {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.tracking_map) as SupportMapFragment
            setupMap(mapFragment)
        }

        setupListeners()

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        firstLoad = true
    }

    private fun setupListeners() {
        binding.buttonStartActivity.setOnClickListener {
            sendActionCommandToService(Constants.ACTION_SERVICE_START_RESUME)
            binding.buttonStartActivity.isVisible = false
            binding.buttonStopActivity.isVisible = true
        }

        binding.buttonStopActivity.setOnClickListener {
            sendActionCommandToService(Constants.ACTION_SERVICE_STOP)
            binding.buttonStartActivity.isVisible = true
            binding.buttonStopActivity.isVisible = false
        }
    }

    private fun sendActionCommandToService(action: String) {
        Intent(this, TrackerService::class.java).run {
            this.action = action
            this@EventActivity.startService(this)
        }
    }

    private fun observeTrackerService() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                TrackerService.locationList.collect { points ->
                    locationList = points
                    if (firstLoad) {
                        removeCurrentPolyline()
                        drawCurrentPolyline()
                    } else if (locationList.size > 1) {
                        drawLastPolyline()
                    }
                    followPolyLine()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                TrackerService.isTracking.collect { isTracking ->
                    if (isTracking) {
                        binding.buttonStartActivity.isVisible = false
                        binding.buttonStopActivity.isVisible = true
                    }
                }
            }
        }
    }

    private fun drawCurrentPolyline() {
        val polyline = googleMap.addPolyline {
            width(DisplayHelper.convertDpToPx(this@EventActivity, Constants.POLYLINE_WIDTH_DP).toFloat())
            color(Color.RED)
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(RoundCap())
            addAll(locationList)
        }
        polylines.add(polyline)
    }

    private fun drawLastPolyline() {
        val polyline = googleMap.addPolyline {
            width(DisplayHelper.convertDpToPx(this@EventActivity, Constants.POLYLINE_WIDTH_DP).toFloat())
            color(Color.RED)
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(RoundCap())
            add(locationList[locationList.lastIndex - 1], locationList[locationList.lastIndex])
        }
        polylines.add(polyline)
    }

    private fun removeCurrentPolyline() {
        polylines.forEach { it.remove() }
        polylines.clear()
    }

    private fun followPolyLine() {
        if (locationList.isNotEmpty()) {
            if (firstLoad) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationList.last(), 17f))
                firstLoad = false
            } else {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(locationList.last()))
            }
        }
    }

    private suspend fun setupMap(mapFragment: SupportMapFragment) {
        googleMap = mapFragment.awaitMap()

        locationClient.lastLocation.addOnCompleteListener {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.result.latitude, it.result.longitude), 14f))
        }

        observeTrackerService()
        googleMap.awaitMapLoad()

        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isRotateGesturesEnabled = false
        }

        googleMap.setMinZoomPreference(10f)
        googleMap.setMaxZoomPreference(20f)
    }
}