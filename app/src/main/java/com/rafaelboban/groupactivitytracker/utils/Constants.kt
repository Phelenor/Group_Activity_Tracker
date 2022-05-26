package com.rafaelboban.groupactivitytracker.utils

object Constants {

    const val ACTION_START = "ACTION_SERVICE_START_RESUME"
    const val ACTION_SERVICE_STOP = "ACTION_SERVICE_STOP"

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

    const val PREFERENCE_EVENT_ID = "eventId"
    const val PREFERENCE_JOINCODE = "joincode"
    const val PREFERENCE_IS_OWNER = "isOwner"

    const val MARKERS_KML_FILENAME = "markers.kml"

    const val LOCATION_UPDATE_INTERVAL = 3000L
    const val LOCATION_UPDATE_INTERVAL_FASTEST = 2000L
    const val TIMER_UPDATE_INTERVAL = 100L

    const val POLYLINE_WIDTH_DP = 4

    const val TYPE_CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
    const val TYPE_LOCATION_DATA = "TYPE_LOCATION_DATA"
    const val TYPE_ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
    const val TYPE_JOIN_HANDSHAKE = "TYPE_JOIN_HANDSHAKE"
    const val TYPE_PHASE_CHANGE = "TYPE_PHASE_CHANGE"
    const val TYPE_PARTICIPANT_LIST = "TYPE_PARTICIPANT_LIST"
    const val TYPE_DISCONNECT_REQUEST = "TYPE_DISCONNECT_REQUEST"

    const val RECONNECT_INTERVAL = 3000L
    const val RECONNECT_INTERVAL_MAX = 9000L

    const val EARTH_RADIUS_METERS = 6371000
}