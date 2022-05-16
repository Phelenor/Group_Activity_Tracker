package com.rafaelboban.groupactivitytracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.Constants.ACTION_SERVICE_START
import com.rafaelboban.groupactivitytracker.utils.Constants.ACTION_SERVICE_STOP
import com.rafaelboban.groupactivitytracker.utils.Constants.NOTIFICATION_ID
import com.rafaelboban.groupactivitytracker.utils.calculateDistance
import com.rafaelboban.groupactivitytracker.utils.calculateDistance2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class TrackerService : LifecycleService() {

    @Inject
    lateinit var notification: NotificationCompat.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var locationClient: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {

        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.forEach {
                updateLocationList(it)
                updateNotification()
            }
        }
    }

    companion object {
        val started = MutableStateFlow(false)
        val locationList = MutableStateFlow<MutableList<LatLng>>(mutableListOf())
    }

    override fun onCreate() {
        locationClient = FusedLocationProviderClient(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_SERVICE_START -> {
                    started.value = true
                    createForegroundChannel()
                    startLocationUpdates()
                }
                ACTION_SERVICE_STOP -> {
                    started.value = false
                    stopForegroundService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun updateNotification() {
        notification.apply {
            setContentTitle("Distance travelled")
            val distance1 = locationList.value.calculateDistance()
            val distance2 = locationList.value.calculateDistance2()
            setContentText("$distance1 km & $distance2 km")
        }
        notificationManager.notify(NOTIFICATION_ID, notification.build())
    }

    private fun stopForegroundService() {
        locationClient.removeLocationUpdates(locationCallback)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        stopForeground(true)
        stopSelf()
    }

    private fun createForegroundChannel() {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, notification.build())
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000L
            fastestInterval = 2000L
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
}