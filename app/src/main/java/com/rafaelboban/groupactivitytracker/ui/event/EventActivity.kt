package com.rafaelboban.groupactivitytracker.ui.event

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.navArgs
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.data.model.Event
import com.rafaelboban.groupactivitytracker.data.socket.DisconnectRequest
import com.rafaelboban.groupactivitytracker.data.socket.JoinEventHandshake
import com.rafaelboban.groupactivitytracker.data.socket.LocationData
import com.rafaelboban.groupactivitytracker.data.socket.PhaseChange
import com.rafaelboban.groupactivitytracker.databinding.ActivityEventBinding
import com.rafaelboban.groupactivitytracker.services.TrackerService
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.DisplayHelper
import com.rafaelboban.groupactivitytracker.utils.IconHelper
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject

const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"

@AndroidEntryPoint
class EventActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEventBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<EventViewModel>()

    private lateinit var googleMap: GoogleMap

    private val args by navArgs<EventActivityArgs>()

    @Inject
    lateinit var locationClient: FusedLocationProviderClient

    @Inject
    lateinit var preferences: SharedPreferences

    private var locationList = emptyList<LatLng>()
    private val polylineList = mutableListOf<Polyline>()
    private var afterOnResume = true

    private var currentPlayerMarker: Marker? = null
    private val playerMarkerMap = HashMap<String, Marker>()

    private var isCameraLocked = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.tracking_map) as SupportMapFragment
            setupMap(mapFragment)
        }

        setupViews()
        setupListeners()
        listenToConnectionEvents()
        listenToSocketEvents()

        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        afterOnResume = true
    }

    private fun setupViews() {
        binding.infoBottomSheet.run {
            joincode.text = args.joincode
            share.setOnClickListener {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Join my group activity with code: ${args.joincode}")
                    type = "text/plain"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            }

            buttonStartActivity.isVisible = args.isOwner
            phaseNote.isVisible = !args.isOwner
        }

        if (isCameraLocked) {
            binding.cameraLockToggle.load(R.drawable.ic_lock)
        } else {
            binding.cameraLockToggle.load(R.drawable.ic_lock_open)
        }
    }

    private fun setupListeners() {
        binding.infoBottomSheet.run {
            buttonStartActivity.setOnClickListener {
                viewModel.sendBaseModel(PhaseChange(Event.Phase.IN_PROGRESS, args.eventId))
                startEvent()
            }

            buttonStopActivity.setOnClickListener {
                finishEvent()
                if (args.isOwner) {
                    viewModel.sendBaseModel(PhaseChange(Event.Phase.FINISHED, args.eventId))
                }
            }

            buttonQuitActivity.setOnClickListener {
                // unsubscribe
                viewModel.sendBaseModel(DisconnectRequest(args.eventId))
                finish()
            }
        }

        binding.cameraLockToggle.setOnClickListener {
            isCameraLocked = !isCameraLocked
            if (isCameraLocked) {
                followPolyline()
                binding.cameraLockToggle.load(R.drawable.ic_lock)
                googleMap.uiSettings.run {
                    isZoomGesturesEnabled = false
                    isScrollGesturesEnabled = false
                }
            } else {
                binding.cameraLockToggle.load(R.drawable.ic_lock_open)
                googleMap.uiSettings.run {
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                }
            }
        }
    }

    private fun sendActionCommandToService(action: String) {
        Intent(this, TrackerService::class.java).run {
            putExtra(EXTRA_EVENT_ID, args.eventId)
            this.action = action
            this@EventActivity.startService(this)
        }
    }

    private fun observeTrackerService() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                TrackerService.locationList.collect { points ->
                    if (points.isNotEmpty()) {
                        locationList = points
                        if (afterOnResume) {
                            removeCurrentPolyline()
                            drawCurrentPolyline()
                        } else if (locationList.size > 1) {
                            drawLastPolyline()
                        }

                        drawPlayerMarker(points.last())
                        if (isCameraLocked) {
                            followPolyline()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TrackerService.isTracking.collect { isTracking ->
                    binding.infoBottomSheet.buttonStopActivity.isVisible = !isTracking
                    binding.infoBottomSheet.buttonStopActivity.isVisible = isTracking
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TrackerService.distance.collect { distance ->
                    val distanceString = "${DecimalFormat("0.00").format(distance)} km"
                    binding.infoBottomSheet.distance.text = distanceString
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TrackerService.timeRunSeconds.collect { time ->
                    binding.infoBottomSheet.time.text = DateUtils.formatElapsedTime(time)
                }
            }
        }
    }

    private fun drawPlayerMarker(latLng: LatLng?) {
        latLng?.let {
            if (currentPlayerMarker == null) {
                val username = preferences.getString(Constants.PREFERENCE_USERNAME, "T")!!
                currentPlayerMarker = googleMap.addMarker {
                    position(it)
                    anchor(0.5f, 0.5f)
                    icon(BitmapDescriptorFactory.fromBitmap(IconHelper.getUserBitmap(this@EventActivity, username)))
                }
            } else {
                currentPlayerMarker?.position = it
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
        polylineList.add(polyline)
    }

    private fun drawLastPolyline() {
        val polyline = googleMap.addPolyline {
            width(DisplayHelper.convertDpToPx(this@EventActivity, Constants.POLYLINE_WIDTH_DP).toFloat())
            color(getColor(R.color.error_red))
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(RoundCap())
            add(locationList[locationList.lastIndex - 1], locationList[locationList.lastIndex])
        }
        polylineList.add(polyline)
    }

    private fun removeCurrentPolyline() {
        polylineList.forEach { it.remove() }
        polylineList.clear()
    }

    private fun followPolyline() {
        if (locationList.isNotEmpty()) {
            if (afterOnResume) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationList.last(), 17f))
                afterOnResume = false
            } else {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(locationList.last()))
            }
        }
    }

    private fun listenToSocketEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.socketEvent.collect { event ->
                    when (event) {
                        is EventViewModel.SocketEvent.ChatMessageEvent -> {

                        }
                        is EventViewModel.SocketEvent.AnnouncementEvent -> {

                        }
                        is EventViewModel.SocketEvent.LocationDataEvent -> {

                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.phase.collect { phaseChange ->
                    Log.d("MARIN", "listenToSocketEvents: $phaseChange")
                    when (phaseChange.phase) {
                        Event.Phase.IN_PROGRESS -> startEvent()
                        Event.Phase.FINISHED -> finishEvent()
                        else -> Unit
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.socketEvent.collect { event ->
                    when (event) {
                        is EventViewModel.SocketEvent.LocationDataEvent -> {
                            saveLocationAndRedraw(event.data)
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun listenToConnectionEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.connectionEvent.collect { event ->
                    when (event) {
                        is WebSocket.Event.OnConnectionOpened<*> -> {
                            val userId = preferences.getString(Constants.PREFERENCE_USER_ID, "")!!
                            val username = preferences.getString(Constants.PREFERENCE_USERNAME, "")!!
                            viewModel.sendBaseModel(JoinEventHandshake(userId, username, args.eventId))
                            Log.i("WS", "Event Handshake")
                        }
                        is WebSocket.Event.OnConnectionFailed -> {
                            Log.i("WS", "Connection Failed")
                        }
                        is WebSocket.Event.OnConnectionClosed -> {
                            Log.i("WS", "Connection Closed")
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun startEvent() {
        sendActionCommandToService(Constants.ACTION_START)
        binding.infoBottomSheet.run {
            buttonStartActivity.isVisible = false
            buttonQuitActivity.isVisible = false
            buttonStopActivity.isVisible = true
            phaseNote.isVisible = false
        }
        binding.cameraLockToggle.isVisible = true
        googleMap.isMyLocationEnabled = false
        googleMap.uiSettings.run {
            isZoomGesturesEnabled = false
            isScrollGesturesEnabled = false
        }
    }

    private fun finishEvent() {
        sendActionCommandToService(Constants.ACTION_SERVICE_STOP)
        binding.infoBottomSheet.run {
            buttonStartActivity.isVisible = false
            buttonQuitActivity.isVisible = true
            buttonStopActivity.isVisible = false
            phaseNote.isVisible = true
            phaseNote.text = "Activity finished."
        }
    }

    private fun saveLocationAndRedraw(location: LocationData) {
        if (playerMarkerMap.containsKey(location.fromUserId)) {
            playerMarkerMap[location.fromUserId]?.remove()
        }

        googleMap.addMarker {
            position(LatLng(location.latitude, location.longitude))
            anchor(0.5f, 0.5f)
            icon(BitmapDescriptorFactory.fromBitmap(IconHelper.getUserBitmap(this@EventActivity, location.fromUsername)))
        }?.also { playerMarkerMap[location.fromUserId] = it }
    }

    private suspend fun setupMap(mapFragment: SupportMapFragment) {
        googleMap = mapFragment.awaitMap()

        locationClient.lastLocation.addOnCompleteListener {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.result.latitude, it.result.longitude), 14f))
        }

        observeTrackerService()

        googleMap.uiSettings.run {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isCompassEnabled = true
            isTiltGesturesEnabled = false
            isRotateGesturesEnabled = false
        }

        googleMap.isMyLocationEnabled = true

        googleMap.setPadding(0, 0, 0, DisplayHelper.convertDpToPx(this, 20))
        googleMap.setMinZoomPreference(10f)
        googleMap.setMaxZoomPreference(20f)

        googleMap.awaitMapLoad()
    }
}