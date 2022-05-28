package com.rafaelboban.groupactivitytracker.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.rafaelboban.groupactivitytracker.data.model.Event
import com.rafaelboban.groupactivitytracker.data.socket.*
import com.rafaelboban.groupactivitytracker.network.ws.EventApi
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventApi: EventApi
) : ViewModel() {

    private val connectionEventChannel = Channel<WebSocket.Event>()
    val connectionEvent = connectionEventChannel.receiveAsFlow().flowOn(Dispatchers.IO)

    private val socketEventChannel = Channel<SocketEvent>()
    val socketEvent = socketEventChannel.receiveAsFlow().flowOn(Dispatchers.IO)

    private val _phase = MutableStateFlow(PhaseChange(Event.Phase.WAITING, ""))
    val phase: StateFlow<PhaseChange> = _phase

    init {
        observeEvents()
        observeBaseModels()
    }

    private fun observeEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            eventApi.observeEvents().collect { event ->
                connectionEventChannel.send(event)
            }
        }
    }

    private fun observeBaseModels() {
        viewModelScope.launch(Dispatchers.IO) {
            eventApi.observeBaseModels().collect { data ->
                when (data) {
                    is ChatMessage -> socketEventChannel.send(SocketEvent.ChatMessageEvent(data))
                    is Announcement -> socketEventChannel.send(SocketEvent.AnnouncementEvent(data))
                    is LocationData -> socketEventChannel.send(SocketEvent.LocationDataEvent(data))
                    is PhaseChange -> _phase.value = PhaseChange(data.phase, data.eventId)
                }
            }
        }
    }

    fun sendBaseModel(data: BaseModel) {
        viewModelScope.launch(Dispatchers.IO) {
            eventApi.sendBaseModel(data)
        }
    }

    sealed class SocketEvent {
        data class ChatMessageEvent(val data: ChatMessage) : SocketEvent()
        data class AnnouncementEvent(val data: Announcement) : SocketEvent()
        data class LocationDataEvent(val data: LocationData) : SocketEvent()
    }
}