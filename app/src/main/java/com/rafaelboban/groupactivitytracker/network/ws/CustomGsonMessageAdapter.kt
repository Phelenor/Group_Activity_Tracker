package com.rafaelboban.groupactivitytracker.network.ws

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.rafaelboban.groupactivitytracker.data.socket.*
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_ANNOUNCEMENT
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_CHAT_MESSAGE
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_DISCONNECT_REQUEST
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_JOIN_HANDSHAKE
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_LOCATION_DATA
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_MARKER_MESSAGE
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_PHASE_CHANGE
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_USER_STATUS
import com.tinder.scarlet.Message
import com.tinder.scarlet.MessageAdapter
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
class CustomGsonMessageAdapter<T> private constructor(
    private val gson: Gson,
) : MessageAdapter<T> {

    override fun fromMessage(message: Message): T {
        val stringValue = when (message) {
            is Message.Text -> message.value
            is Message.Bytes -> message.value.toString()
        }
        val jsonObject = JsonParser.parseString(stringValue).asJsonObject
        val type = when (jsonObject.get("type").asString) {
            TYPE_JOIN_HANDSHAKE -> JoinEventHandshake::class.java
            TYPE_CHAT_MESSAGE -> ChatMessage::class.java
            TYPE_LOCATION_DATA -> LocationData::class.java
            TYPE_ANNOUNCEMENT -> Announcement::class.java
            TYPE_PHASE_CHANGE -> PhaseChange::class.java
            TYPE_DISCONNECT_REQUEST -> DisconnectRequest::class.java
            TYPE_MARKER_MESSAGE -> MarkerMessage::class.java
            TYPE_USER_STATUS -> ParticipantDataList::class.java
            else -> BaseModel::class.java
        }
        val obj = gson.fromJson(stringValue, type)
        return obj as T
    }

    override fun toMessage(data: T): Message {
        var convertedData = data as BaseModel
        convertedData = when (convertedData.type) {
            TYPE_JOIN_HANDSHAKE -> convertedData as JoinEventHandshake
            TYPE_CHAT_MESSAGE -> convertedData as ChatMessage
            TYPE_LOCATION_DATA -> convertedData as LocationData
            TYPE_ANNOUNCEMENT -> convertedData as Announcement
            TYPE_PHASE_CHANGE -> convertedData as PhaseChange
            TYPE_DISCONNECT_REQUEST -> convertedData as DisconnectRequest
            TYPE_MARKER_MESSAGE -> convertedData as MarkerMessage
            TYPE_USER_STATUS -> convertedData as ParticipantDataList
            else -> convertedData
        }
        return Message.Text(gson.toJson(convertedData))
    }

    class Factory(private val gson: Gson) : MessageAdapter.Factory {

        override fun create(type: Type, annotations: Array<Annotation>): MessageAdapter<*> {
            return CustomGsonMessageAdapter<Any>(gson)
        }
    }
}