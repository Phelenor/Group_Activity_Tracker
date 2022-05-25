package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_DISCONNECT_REQUEST

class DisconnectRequest(
    val eventId: String
) : BaseModel(TYPE_DISCONNECT_REQUEST)