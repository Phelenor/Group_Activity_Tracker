package com.rafaelboban.groupactivitytracker.utils

object Constants {

    const val ACTION_SERVICE_START_RESUME = "ACTION_SERVICE_START_RESUME"
    const val ACTION_SERVICE_STOP = "ACTION_SERVICE_STOP"
    const val ACTION_SERVICE_PAUSE = "ACTION_SERVICE_PAUSE"

    const val NOTIFICATION_CHANNEL_ID = "tracker_notification_id"
    const val NOTIFICATION_CHANNEL_NAME = "tracker_notification"
    const val NOTIFICATION_ID = 100

    const val PENDING_INTENT_REQUEST_CODE = 0

    const val PASSWORD_REGEX_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$"

    const val URL_LOCALHOST = "http://10.0.2.2:8080"
    const val PREFERENCES_NAME = "TrackerPreferences"
    const val PREFERENCE_JWT_TOKEN = "JWT_TOKEN"

    const val PREFERENCE_USER_ID = "PREF_USER_ID"
    const val PREFERENCE_USERNAME = "PREF_USERNAME"
    const val PREFERENCE_EMAIL = "PREF_EMAIL"
    const val PREFERENCE_MARKER_TOOLTIP_SHOWN = "PREFERENCE_MARKER_TOOLTIP_SHOWN"

    const val MARKERS_KML_FILENAME = "markers.kml"

    const val LOCATION_UPDATE_INTERVAL = 3000L
    const val LOCATION_UPDATE_INTERVAL_FASTEST = 2000L

    const val POLYLINE_WIDTH_DP = 4
}