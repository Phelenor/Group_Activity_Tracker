package com.rafaelboban.groupactivitytracker.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.ui.event.EventActivity
import com.rafaelboban.groupactivitytracker.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    @ServiceScoped
    fun providePendingIntent(@ApplicationContext context: Context, preferences: SharedPreferences): PendingIntent {
        val eventId = preferences.getString(Constants.PREFERENCE_EVENT_ID, "")!!
        val joincode = preferences.getString(Constants.PREFERENCE_JOINCODE, "")!!
        val isOwner = preferences.getBoolean(Constants.PREFERENCE_IS_OWNER, false)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_REQUEST_CODE,
                Intent(context, EventActivity::class.java).apply {
                    putExtra(Constants.PREFERENCE_EVENT_ID, eventId)
                    putExtra(Constants.PREFERENCE_JOINCODE, joincode)
                    putExtra(Constants.PREFERENCE_IS_OWNER, isOwner)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(context,
                Constants.PENDING_INTENT_REQUEST_CODE,
                Intent(context, EventActivity::class.java).apply {
                    putExtra(Constants.PREFERENCE_EVENT_ID, eventId)
                    putExtra(Constants.PREFERENCE_JOINCODE, joincode)
                    putExtra(Constants.PREFERENCE_IS_OWNER, isOwner)
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