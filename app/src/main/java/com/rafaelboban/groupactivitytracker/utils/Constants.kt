package com.rafaelboban.groupactivitytracker.utils

object Constants {

    const val ACTION_SERVICE_START = "ACTION_SERVICE_START_RESUME"
    const val ACTION_SERVICE_STOP = "ACTION_SERVICE_STOP"

    const val NOTIFICATION_CHANNEL_ID = "tracker_notification_id"
    const val NOTIFICATION_CHANNEL_NAME = "tracker_notification"
    const val NOTIFICATION_ID = 100

    const val PENDING_INTENT_REQUEST_CODE = 0

    const val PASSWORD_REGEX_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$"

    const val PREFERENCES_NAME = "TrackerPreferences"
    const val ENCRYPTED_PREFERENCES_NAME = "EncryptedTrackerPreferences"
    const val PREFERENCE_JWT_TOKEN = "JWT_TOKEN"

    const val PREFERENCE_USER_ID = "PREF_USER_ID"
    const val PREFERENCE_USERNAME = "PREF_USERNAME"
    const val PREFERENCE_EMAIL = "PREF_EMAIL"

    const val PREFERENCE_EVENT_ID = "eventId"
    const val PREFERENCE_JOINCODE = "joincode"
    const val PREFERENCE_IS_OWNER = "isOwner"

    const val LOCATION_UPDATE_INTERVAL = 3000L
    const val LOCATION_UPDATE_INTERVAL_FASTEST = 2500L

    const val POLYLINE_WIDTH_DP = 4

    const val TYPE_CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
    const val TYPE_LOCATION_DATA = "TYPE_LOCATION_DATA"
    const val TYPE_ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
    const val TYPE_JOIN_HANDSHAKE = "TYPE_JOIN_HANDSHAKE"
    const val TYPE_PHASE_CHANGE = "TYPE_PHASE_CHANGE"
    const val TYPE_DISCONNECT_REQUEST = "TYPE_DISCONNECT_REQUEST"
    const val TYPE_FINISH_EVENT = "TYPE_FINISH_EVENT"
    const val TYPE_MARKER_MESSAGE = "TYPE_MARKER_MESSAGE"
    const val TYPE_USER_STATUS = "TYPE_USER_STATUS"

    const val RECONNECT_INTERVAL = 3000L
    const val RECONNECT_INTERVAL_MAX = 9000L

    const val API_URL = "https://group-activity-tracker.herokuapp.com"

}