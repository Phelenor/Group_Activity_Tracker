package com.rafaelboban.groupactivitytracker.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.request.MarkerRequest
import com.rafaelboban.groupactivitytracker.data.socket.*
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.network.ws.EventApi
import com.rafaelboban.groupactivitytracker.utils.executeRequest
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventApi: EventApi,
    private val apiService: ApiService,
) : ViewModel() {

    private val connectionEventChannel = Channel<WebSocket.Event>()
    val connectionEvent = connectionEventChannel.receiveAsFlow().flowOn(Dispatchers.IO)

    private val socketEventChannel = Channel<SocketEvent>()
    val socketEvent = socketEventChannel.receiveAsFlow().flowOn(Dispatchers.IO)

    private val _phase = Channel<PhaseChange>()
    val phase = _phase.receiveAsFlow().flowOn(Dispatchers.IO)

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
                    is MarkerMessage -> socketEventChannel.send(SocketEvent.MarkerMessageEvent(data))
                    is ParticipantDataList -> socketEventChannel.send(SocketEvent.ParticipantListEvent(data))
                    is PhaseChange -> _phase.send(PhaseChange(data.phase, data.eventId))
                }
            }
        }
    }

    fun sendBaseModel(data: BaseModel) {
        viewModelScope.launch(Dispatchers.IO) {
            eventApi.sendBaseModel(data)
        }
    }

    fun saveMarker(eventId: String, latitude: Double, longitude: Double, title: String, snippet: String) {
        viewModelScope.launch {
            val request = MarkerRequest(eventId, latitude, longitude, title, snippet)
            executeRequest { apiService.saveMarker(request) }
        }
    }

    sealed class SocketEvent {
        data class ChatMessageEvent(val data: ChatMessage) : SocketEvent()
        data class AnnouncementEvent(val data: Announcement) : SocketEvent()
        data class LocationDataEvent(val data: LocationData) : SocketEvent()
        data class MarkerMessageEvent(val data: MarkerMessage) : SocketEvent()
        data class ParticipantListEvent(val data: ParticipantDataList) : SocketEvent()
    }
}