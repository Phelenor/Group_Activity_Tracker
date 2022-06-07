package com.rafaelboban.groupactivitytracker.ui.event

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.google.maps.android.ktx.utils.sphericalDistance
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.data.model.Event
import com.rafaelboban.groupactivitytracker.data.model.ParticipantData
import com.rafaelboban.groupactivitytracker.data.model.ParticipantData.Status.FINISHED
import com.rafaelboban.groupactivitytracker.data.model.ParticipantData.Status.LEFT
import com.rafaelboban.groupactivitytracker.data.socket.*
import com.rafaelboban.groupactivitytracker.databinding.ActivityEventBinding
import com.rafaelboban.groupactivitytracker.di.AppModule
import com.rafaelboban.groupactivitytracker.services.TrackerService
import com.rafaelboban.groupactivitytracker.ui.event.adapter.ChatAdapter
import com.rafaelboban.groupactivitytracker.ui.event.adapter.MarkerInfoAdapter
import com.rafaelboban.groupactivitytracker.ui.event.adapter.ParticipantInfoAdapter
import com.rafaelboban.groupactivitytracker.utils.*
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.math.roundToInt

const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"


@AndroidEntryPoint
class EventActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEventBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<EventViewModel>()

    private val participantAdapter by lazy { ParticipantInfoAdapter(this) }

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var googleMap: GoogleMap

    private val args by navArgs<EventActivityArgs>()

    @Inject
    lateinit var locationClient: FusedLocationProviderClient

    @Inject
    @AppModule.PreferencesStandard
    lateinit var preferences: SharedPreferences

    private var locationList = emptyList<LocationData>()
    private val polylineList = mutableListOf<Polyline>()
    private var afterOnResume = true

    private var currentPlayerMarker: Marker? = null
    private val playerMarkerMap = HashMap<String, Marker>()

    private var isCameraLocked = true

    private var connectedToEvent = false

    private var updateChatJob: Job? = null

    private lateinit var userId: String
    private lateinit var username: String

    private var currentMarkerLatLng: LatLng? = null
    private var currentMarkerClicked: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userId = preferences.getString(Constants.PREFERENCE_USER_ID, "")!!
        username = preferences.getString(Constants.PREFERENCE_USERNAME, "")!!

        lifecycleScope.launchWhenCreated {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.tracking_map) as SupportMapFragment
            setupMap(mapFragment)
        }

        CoroutineScope(Dispatchers.Main).launch {
            if (!connectedToEvent) {
                viewModel.sendBaseModel(JoinEventHandshake(userId, username, args.eventId))
                connectedToEvent = true
            }
        }

        setupViews()
        setupChatBottomSheet()
        setupListeners()
        listenToConnectionEvents()
        listenToSocketEvents()

        setContentView(binding.root)
    }

    private fun setupChatBottomSheet() {
        chatAdapter = ChatAdapter(this, userId)

        binding.chatBottomSheet.run {
            recyclerView.layoutManager = LinearLayoutManager(this@EventActivity)
            recyclerView.adapter = chatAdapter

            sendLayout.send.setOnClickListener {
                val message = ChatMessage(userId,
                    username,
                    args.eventId,
                    sendLayout.enterMessage.text.toString(),
                    System.currentTimeMillis()
                )

                if (message.message.trim().isEmpty()) return@setOnClickListener
                sendLayout.enterMessage.text?.clear()
                viewModel.sendBaseModel(message)
            }
        }
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
                sendActionCommandToService(Constants.ACTION_SERVICE_START)
                observeTrackerService()
                adjustViewsForStart()
            }

            buttonStopActivity.setOnClickListener {
                sendActionCommandToService(Constants.ACTION_SERVICE_STOP)

                if (args.isOwner) {
                    adjustViewsForFinish()
                    viewModel.sendBaseModel(PhaseChange(Event.Phase.FINISHED, args.eventId))
                } else {
                    adjustViewsForStop()
                }

                currentPlayerMarker?.remove()
                googleMap.isMyLocationEnabled = true
                viewModel.sendBaseModel(
                    FinishEvent(
                        args.eventId,
                        userId,
                        username,
                        TrackerService.distance.value
                    )
                )
            }

            buttonQuitActivity.setOnClickListener {
                TrackerService.resetStaticData()
                viewModel.sendBaseModel(DisconnectRequest(args.eventId, username))
                preferences.removeEventData()
                finish()
            }

            buttonHelp.setOnClickListener {
                MaterialAlertDialogBuilder(this@EventActivity)
                    .setTitle(resources.getString(R.string.help_confirm))
                    .setNeutralButton(resources.getString(R.string.dismiss_lower)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                        TrackerService.needsHelp = true
                        viewModel.sendBaseModel(Announcement(
                            args.eventId,
                            "$username needs help!",
                            System.currentTimeMillis(),
                            Announcement.TYPE_PLAYER_HELP)
                        )
                        buttonHelp.isVisible = false
                        buttonDismissHelpStatus.isVisible = true
                        dialog.dismiss()
                    }
                    .show()
            }

            buttonDismissHelpStatus.setOnClickListener {
                MaterialAlertDialogBuilder(this@EventActivity)
                    .setTitle(resources.getString(R.string.help_dismiss_confirm))
                    .setNeutralButton(resources.getString(R.string.dismiss_lower)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(resources.getString(R.string.yes)) { dialog, _ ->
                        TrackerService.needsHelp = false
                        viewModel.sendBaseModel(Announcement(
                            args.eventId,
                            "$username is OK!",
                            System.currentTimeMillis(),
                            Announcement.TYPE_PLAYER_HELP_CLEAR)
                        )
                        buttonHelp.isVisible = true
                        buttonDismissHelpStatus.isVisible = false
                        dialog.dismiss()
                    }
                    .show()
            }
        }

        binding.infoButton.setOnClickListener {
            DialogHelper.showParticipantInfoDialog(this, participantAdapter)
        }

        binding.mapTypeChange.setOnClickListener {
            DialogHelper.showSelectMarkerTypeDialog(this, googleMap) {
                if (googleMap.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                    setDefaultButtonColors()
                } else {
                    setSecondaryButtonColors()
                }
            }
        }

        binding.cameraLockToggle.setOnClickListener {
            if (isCameraLocked) {
                unlockCamera()
            } else {
                lockCamera()
            }
        }

        binding.cameraSpreadButton.setOnClickListener {
            if (isCameraLocked) {
                unlockCamera()
            }
            spreadCameraToAllParticipants()
        }
    }

    private fun lockCamera() {
        isCameraLocked = true
        binding.cameraLockToggle.load(R.drawable.ic_lock)
        googleMap.uiSettings.run {
            isZoomGesturesEnabled = false
            isScrollGesturesEnabled = false
        }
        followPolyline()
    }

    private fun unlockCamera() {
        isCameraLocked = false
        binding.cameraLockToggle.load(R.drawable.ic_lock_open)
        googleMap.uiSettings.run {
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
        }
    }

    private fun spreadCameraToAllParticipants() {
        if (TrackerService.locationList.value.isNotEmpty() && playerMarkerMap.isNotEmpty()) {
            val points =
                playerMarkerMap.values.map { it.position } + TrackerService.locationList.value.last()
                    .let { LatLng(it.latitude, it.longitude) }
            val padding = DisplayHelper.convertDpToPx(this, 32)
            val bounds = LatLngBounds.builder().run {
                points.forEach { latLng -> include(latLng) }
                build()
            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
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

                        drawUserMarker(points.last())
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
                    val distanceString = if (distance < 1) {
                        "${(distance * 1000).roundToInt()} m"
                    } else {
                        "${DecimalFormat("0.00").format(distance)} km"
                    }
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

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TrackerService.speed.collect { speed ->
                    val display = "${DecimalFormat("0.00").format(speed)} km/h"
                    binding.infoBottomSheet.speed.text = display
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TrackerService.direction.collect { direction ->
                    binding.infoBottomSheet.direction.text = direction
                }
            }
        }
    }

    private fun listenToSocketEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.phase.collect { phaseChange ->
                    when (phaseChange.phase) {
                        Event.Phase.IN_PROGRESS -> {
                            sendActionCommandToService(Constants.ACTION_SERVICE_START)
                            observeTrackerService()
                            adjustViewsForStart()
                        }
                        Event.Phase.FINISHED -> {
                            adjustViewsForFinish()
                            if (TrackerService.isTracking.value) {
                                sendActionCommandToService(Constants.ACTION_SERVICE_STOP)
                                viewModel.sendBaseModel(
                                    FinishEvent(
                                        args.eventId,
                                        userId,
                                        username,
                                        TrackerService.distance.value
                                    )
                                )
                            }
                        }
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
                        is EventViewModel.SocketEvent.ChatMessageEvent -> {
                            addChatObjectToList(event.data)
                        }
                        is EventViewModel.SocketEvent.AnnouncementEvent -> {
                            addChatObjectToList(event.data)
                        }
                        is EventViewModel.SocketEvent.MarkerMessageEvent -> {
                            addMessageMarkerToMap(event.data)
                        }
                        is EventViewModel.SocketEvent.ParticipantListEvent -> {
                            checkMarkersAndUpdateParticipantList(event.data.list)
                        }
                    }
                }
            }
        }
    }

    private fun checkMarkersAndUpdateParticipantList(participantList: List<ParticipantData>) {
        participantAdapter.updateItems(participantList)
        CoroutineScope(Dispatchers.Main).launch {
            val finishedAndLeft = participantList.filter { it.status in setOf(FINISHED, LEFT) }.map { it.id }
            playerMarkerMap.keys.forEach { userId ->
                if (userId in finishedAndLeft) {
                    playerMarkerMap[userId]?.remove()
                    playerMarkerMap.remove(userId)
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
                            viewModel.sendBaseModel(JoinEventHandshake(userId, username, args.eventId))
                            connectedToEvent = true
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

    private fun setDefaultButtonColors() {
        binding.run {
            val color = getColor(R.color.md_theme_light_primary)
            infoButton.setColorFilter(color)
            mapTypeChange.setColorFilter(color)
            cameraLockToggle.setColorFilter(color)
            cameraSpreadButton.setColorFilter(color)
        }
    }

    private fun setSecondaryButtonColors() {
        binding.run {
            val color = getColor(R.color.light_orange)
            infoButton.setColorFilter(color)
            mapTypeChange.setColorFilter(color)
            cameraLockToggle.setColorFilter(color)
            cameraSpreadButton.setColorFilter(color)
        }
    }

    private fun adjustViewsForStart() {
        binding.infoBottomSheet.run {
            buttonStartActivity.isVisible = false
            buttonQuitActivity.isVisible = false
            buttonStopActivity.isVisible = true
            phaseNote.isVisible = false
            joincodeTitle.isVisible = false
            joincode.isVisible = false
            share.isVisible = false
            buttonHelp.isVisible = true

            if (TrackerService.needsHelp) {
                buttonHelp.isVisible = false
                buttonDismissHelpStatus.isVisible = true
            }
        }
        binding.cameraLockToggle.isVisible = true
        binding.cameraSpreadButton.isVisible = true
        googleMap.isMyLocationEnabled = false
        googleMap.uiSettings.run {
            isZoomGesturesEnabled = false
            isScrollGesturesEnabled = false
        }
    }

    private fun adjustViewsForStop() {
        binding.infoBottomSheet.run {
            buttonStartActivity.isVisible = false
            buttonQuitActivity.isVisible = true
            buttonStopActivity.isVisible = false
            buttonHelp.isVisible = false
            buttonDismissHelpStatus.isVisible = false
            phaseNote.isVisible = true
            phaseNote.text = getString(R.string.activity_finished_current)
        }
    }

    private fun adjustViewsForFinish() {
        binding.infoBottomSheet.run {
            buttonStartActivity.isVisible = false
            buttonQuitActivity.isVisible = true
            buttonStopActivity.isVisible = false
            buttonHelp.isVisible = false
            buttonDismissHelpStatus.isVisible = false
            phaseNote.isVisible = true
            speedTitle.isVisible = false
            speed.isVisible = false
            directionTitle.isVisible = false
            direction.isVisible = false
            dividerBottom.isVisible = false
            phaseNote.text = getString(R.string.activity_finished)
        }

        binding.cameraLockToggle.isVisible = false
        binding.cameraSpreadButton.isVisible = false
        unlockCamera()
        if (TrackerService.locationList.value.isNotEmpty()) {
            val padding = DisplayHelper.convertDpToPx(this, 32)
            val bounds = LatLngBounds.builder().run {
                TrackerService.locationList.value.forEach { location -> include(LatLng(location.latitude, location.longitude)) }
                build()
            }

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }

    private fun updateChatMessageList(messages: List<BaseModel>) {
        updateChatJob?.cancel()
        updateChatJob = lifecycleScope.launch {
            chatAdapter.updateItems(messages)
        }
    }

    private suspend fun addChatObjectToList(chatObject: BaseModel) {
        val canScrollDown = binding.chatBottomSheet.recyclerView.canScrollVertically(1)
        updateChatMessageList(chatAdapter.chatItems + chatObject)
        updateChatJob?.join()
        if (!canScrollDown) {
            binding.chatBottomSheet.recyclerView.scrollToPosition(chatAdapter.chatItems.size - 1)
        }
    }

    private fun addMessageMarkerToMap(markerData: MarkerMessage) {
        googleMap.addMarker {
            position(LatLng(markerData.latitude, markerData.longitude))
            title(markerData.title)
            snippet(markerData.snippet)
        }
    }

    private fun drawUserMarker(location: LocationData) {
        val latLng = LatLng(location.latitude, location.longitude)
        currentPlayerMarker?.remove()
        currentPlayerMarker = googleMap.addMarker {
            position(latLng)
            anchor(0.5f, 0.5f)
            icon(BitmapDescriptorFactory.fromBitmap(
                IconHelper.getUserBitmap(
                    this@EventActivity,
                    username,
                    true,
                    location.needsHelp))
            )
        }
    }

    private fun drawCurrentPolyline() {
        val polyline = googleMap.addPolyline {
            width(DisplayHelper.convertDpToPx(this@EventActivity, Constants.POLYLINE_WIDTH_DP).toFloat())
            color(getColor(R.color.polyline_purple))
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(RoundCap())
            addAll(locationList.map { LatLng(it.latitude, it.longitude) })
        }
        polylineList.add(polyline)
    }

    private fun drawLastPolyline() {
        val nextToLast = locationList[locationList.lastIndex - 1]
        val last = locationList[locationList.lastIndex]
        val polyline = googleMap.addPolyline {
            width(DisplayHelper.convertDpToPx(this@EventActivity, Constants.POLYLINE_WIDTH_DP).toFloat())
            color(getColor(R.color.polyline_purple))
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(RoundCap())
            add(LatLng(nextToLast.latitude, nextToLast.longitude), LatLng(last.latitude, last.longitude))
        }
        polylineList.add(polyline)
    }

    private fun removeCurrentPolyline() {
        polylineList.forEach { it.remove() }
        polylineList.clear()
    }

    private fun followPolyline() {
        if (locationList.isNotEmpty()) {
            val last = locationList.last()
            if (afterOnResume) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(last.latitude, last.longitude), 17f))
                afterOnResume = false
            } else {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(LatLng(last.latitude, last.longitude)))
            }
        }
    }

    private fun saveLocationAndRedraw(location: LocationData) {
        var isInfoWindowShown = false
        if (playerMarkerMap.containsKey(location.fromUserId)) {
            isInfoWindowShown = playerMarkerMap[location.fromUserId]!!.isInfoWindowShown
            playerMarkerMap[location.fromUserId]?.remove()
        }

        val latLng = LatLng(location.latitude, location.longitude)
        val distanceBetween = if (locationList.isNotEmpty()) {
            val userLastLocation = locationList.last()
            val latLngUser = LatLng(userLastLocation.latitude, userLastLocation.longitude)
            latLngUser.sphericalDistance(latLng) / 1000
        } else 0.0

        val data = MarkerInfoAdapter.MarkerData(
            location.fromUsername,
            location.distance,
            location.speed,
            location.direction,
            distanceBetween
        )
        val jsonDataString = Gson().toJson(data)

        googleMap.addMarker {
            position(latLng)
            anchor(0.5f, 0.5f)
            snippet(jsonDataString)
            icon(BitmapDescriptorFactory.fromBitmap(
                IconHelper.getUserBitmap(
                    this@EventActivity,
                    location.fromUsername,
                    false,
                    location.needsHelp))
            )
        }?.also {
            playerMarkerMap[location.fromUserId] = it
            if (isInfoWindowShown) it.showInfoWindow()
        }
    }

    private suspend fun setupMap(mapFragment: SupportMapFragment) {
        googleMap = mapFragment.awaitMap()

        locationClient.lastLocation.addOnCompleteListener { location ->
            if (location.isComplete && location.result != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.result.latitude, location.result.longitude), 14f))
            }
        }

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

        googleMap.setInfoWindowAdapter(MarkerInfoAdapter(this))

        if (TrackerService.isTracking.value) {
            observeTrackerService()
            adjustViewsForStart()
        }

        googleMap.setOnMapLongClickListener { latLng -> onMapLongClick(latLng) }
        googleMap.setOnMarkerClickListener { marker -> onMarkerClick(marker) }
        googleMap.awaitMapLoad()
    }

    private fun onMapLongClick(latLng: LatLng) {
        VibrationHelper.vibrate(this)
        currentMarkerLatLng = latLng
        DialogHelper.showCreateMarkerDialog(
            this,
            googleMap,
            currentMarkerLatLng!!,
            args.eventId,
            username,
            { eventId, latitude, longitude, title, snippet -> viewModel.saveMarker(eventId, latitude, longitude, title, snippet) },
            { baseModel -> viewModel.sendBaseModel(baseModel) },
        )
    }

    private fun onMarkerClick(marker: Marker): Boolean {
        if (marker.title.isNullOrEmpty() && !marker.snippet.isNullOrEmpty()) {
            marker.showInfoWindow()
        } else if (marker.title.isNullOrEmpty().not()) {
            currentMarkerClicked = marker
            DialogHelper.showMarkerViewDialog(this, currentMarkerClicked!!) {
                currentMarkerClicked!!.remove()
                currentMarkerClicked = null
            }
        } else {
            return false
        }
        return true
    }
}