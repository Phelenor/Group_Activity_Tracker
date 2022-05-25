package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_ANNOUNCEMENT

data class Announcement(
    val message: String,
    val timestamp: Long,
    val announcementType: Int,
) : BaseModel(TYPE_ANNOUNCEMENT) {

    companion object {
        const val TYPE_PLAYER_JOINED = 0
        const val TYPE_PLAYER_LEFT = 1
        const val TYPE_PLAYER_SOS = 2
    }
}