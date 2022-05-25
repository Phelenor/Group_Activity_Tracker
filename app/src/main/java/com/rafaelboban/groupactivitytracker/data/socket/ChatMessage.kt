package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_CHAT_MESSAGE


data class ChatMessage(
    val from: String,
    val eventId: String,
    val message: String,
    val timestamp: Long
) : BaseModel(TYPE_CHAT_MESSAGE)
