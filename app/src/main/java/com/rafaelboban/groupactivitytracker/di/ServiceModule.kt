package com.rafaelboban.groupactivitytracker.di

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.network.ws.CustomGsonMessageAdapter
import com.rafaelboban.groupactivitytracker.network.ws.EventApi
import com.rafaelboban.groupactivitytracker.network.ws.FlowStreamAdapter
import com.rafaelboban.groupactivitytracker.ui.event.EventActivity
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.retry.ExponentialBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun providePendingIntent(@ApplicationContext context: Context): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_REQUEST_CODE,
                Intent(context, EventActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_REQUEST_CODE,
                Intent(context, EventActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    @Provides
    @ServiceScoped
    fun provideNotificationBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent,
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run)
            .setContentIntent(pendingIntent)
            .setContentTitle(context.getString(R.string.app_name))
    }

    @Provides
    @ServiceScoped
    fun provideNotificationManager(
        @ApplicationContext context: Context,
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}