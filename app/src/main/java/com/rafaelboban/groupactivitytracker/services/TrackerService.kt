package com.rafaelboban.groupactivitytracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.text.format.DateUtils
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.rafaelboban.groupactivitytracker.network.ApiService
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.Constants.ACTION_SERVICE_START_RESUME
import com.rafaelboban.groupactivitytracker.utils.Constants.ACTION_SERVICE_STOP
import com.rafaelboban.groupactivitytracker.utils.Constants.NOTIFICATION_ID
import com.rafaelboban.groupactivitytracker.utils.calculateDistance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.DecimalFormat
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.toJavaDuration

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var api: ApiService

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var locationClient: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.forEach {
                updateLocationList(it)
//                lifecycleScope.launchWhenCreated {
//                    val request = com.rafaelboban.groupactivitytracker.data.request.LocationRequest(it.latitude,
//                        it.longitude,
//                        System.currentTimeMillis())
//                    safeResponse { api.saveLocation(request) }
//                }
            }
        }
    }


    private lateinit var timerJob: Job
    private var timestampStart = 0L
    private var timestampEnd = 0L

    companion object {
        val isTracking = MutableStateFlow(false)
        val locationList = MutableStateFlow<MutableList<LatLng>>(mutableListOf())
        val distance = MutableStateFlow(0.0)
        val timeRunSeconds = MutableStateFlow(0L)
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = FusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SERVICE_START_RESUME -> {
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
        val distanceString = DecimalFormat("0.00").format(Companion.distance.value)
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
        val latLng = LatLng(location.latitude, location.longitude)
        locationList.value = (locationList.value + latLng) as MutableList<LatLng>
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
                updateNotification()
                delay(1000L)
            }
        }
    }
}