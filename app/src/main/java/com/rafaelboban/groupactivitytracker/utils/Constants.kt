package com.rafaelboban.groupactivitytracker.utils

object Constants {

    const val PERMISSION_LOCATION_REQUEST_CODE = 1
    const val PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE = 2

    const val ACTION_SERVICE_START = "ACTION_SERVICE_START"
    const val ACTION_SERVICE_STOP = "ACTION_SERVICE_STOP"

    const val NOTIFICATION_CHANNEL_ID = "tracker_notification_id"
    const val NOTIFICATION_CHANNEL_NAME = "tracker_notification"
    const val NOTIFICATION_ID = 100

    const val PENDING_INTENT_REQUEST_CODE = 0

    const val PASSWORD_REGEX_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$"

    const val URL_LOCALHOST = "http://10.0.2.2:8080"
    const val PREFERENCES_NAME = "TrackerPreferences"
}