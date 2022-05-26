package com.rafaelboban.groupactivitytracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.text.format.DateUtils
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.rafaelboban.groupactivitytracker.data.socket.LocationData
import com.rafaelboban.groupactivitytracker.network.ws.EventApi
import com.rafaelboban.groupactivitytracker.ui.event.EXTRA_EVENT_ID
import com.rafaelboban.groupactivitytracker.utils.*
import com.rafaelboban.groupactivitytracker.utils.Constants.ACTION_SERVICE_STOP
import com.rafaelboban.groupactivitytracker.utils.Constants.ACTION_SERVICE_START
import com.rafaelboban.groupactivitytracker.utils.Constants.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var eventApi: EventApi

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var preferences: SharedPreferences

    private var eventId: String = ""

    private lateinit var locationClient: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.forEach {
                updateLocationList(it)
            }
        }
    }


    private lateinit var timerJob: Job
    private var timestampStart = 0L
    private var timestampEnd = 0L

    companion object {
        val isTracking = MutableStateFlow(false)
        val locationList = MutableStateFlow<MutableList<LocationData>>(mutableListOf())
        val distance = MutableStateFlow(0.0)
        val speed = MutableStateFlow(-1.0)
        val direction = MutableStateFlow("-")
        val timeRunSeconds = MutableStateFlow(0L)

        fun resetStaticData() {
            CoroutineScope(Dispatchers.Main).launch {
                delay(250L)
                locationList.value.clear()
                distance.value = 0.0
                speed.value = 0.0
                timeRunSeconds.value = 0L
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = FusedLocationProviderClient(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SERVICE_START -> {
                    eventId = it.extras?.get(EXTRA_EVENT_ID) as String
                    isTracking.value = true
                    startForegroundService()
                    startLocationUpdates()
                    startTimer()
                    timestampStart = System.currentTimeMillis()
                }
                ACTION_SERVICE_STOP -> {
                    isTracking.value = false
                    stopForegroundService()
                    timestampEnd = System.currentTimeMillis()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun stopForegroundService() {
        locationClient.removeLocationUpdates(locationCallback)
        notificationManager.cancel(NOTIFICATION_ID)
        timerJob.cancel()
        stopForeground(true)
        stopSelf()
    }

    private fun updateNotification() {
        val durationString = DateUtils.formatElapsedTime(timeRunSeconds.value)
        val distanceString = DecimalFormat("0.00").format(distance.value)
        notificationBuilder.setContentText("$durationString | $distanceString km")
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = Constants.LOCATION_UPDATE_INTERVAL
            fastestInterval = Constants.LOCATION_UPDATE_INTERVAL_FASTEST
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun updateLocationList(location: Location) {
        val userId = preferences.getString(Constants.PREFERENCE_USER_ID, "")!!
        val username = preferences.getString(Constants.PREFERENCE_USERNAME, "")!!
        val locationData = LocationData(userId,
            username,
            eventId,
            location.latitude,
            location.longitude,
            distance.value,
            speed.value,
            direction.value,
            System.currentTimeMillis()
        )
        locationList.value = (locationList.value + locationData) as MutableList<LocationData>
        eventApi.sendBaseModel(locationData)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value) {
                timeRunSeconds.value = timeRunSeconds.value.plus(1)
                distance.value = locationList.value.calculateDistance()
                if (locationList.value.size > 1) {
                    val lastPoints = locationList.value.takeLast(2)
                    speed.value = lastPoints.calculateSpeed()
                    direction.value = lastPoints.getDirection().value
                }
                updateNotification()
                delay(1000L)
            }
        }
    }
}