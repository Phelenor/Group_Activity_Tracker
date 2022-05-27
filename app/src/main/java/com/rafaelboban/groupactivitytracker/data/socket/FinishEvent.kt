package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_FINISH_EVENT

data class FinishEvent(
    val eventId: String,
    val userId: String,
    val username: String,
) : BaseModel(TYPE_FINISH_EVENT)