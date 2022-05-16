package com.rafaelboban.groupactivitytracker.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rafaelboban.ui.MainActivity
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object NotificationModule {

    @Provides
    @ServiceScoped
    fun providerPendingIntent(@ApplicationContext context: Context): PendingIntent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_REQUEST_CODE,
                Intent(context, MainActivity::class.java).apply {
                    this.action = Constants.ACTION_NAVIGATE_TO_MAPS_FRAGMENT
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_REQUEST_CODE,
                Intent(context, MainActivity::class.java).apply {
                    this.action = Constants.ACTION_NAVIGATE_TO_MAPS_FRAGMENT
                },
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
    }

    @Provides
    @ServiceScoped
    fun providerNotificationManager(
        @ApplicationContext context: Context,
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}