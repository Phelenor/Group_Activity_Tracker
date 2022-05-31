package com.rafaelboban.groupactivitytracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.data.request.CreateEventRequest
import com.rafaelboban.groupactivitytracker.data.request.DeleteMarkerRequest
import com.rafaelboban.groupactivitytracker.data.request.JoinEventRequest
import com.rafaelboban.groupactivitytracker.data.request.MarkerRequest
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val api: ApiService,
) : ViewModel() {

    var isActivityListInitialized = false

    private val _eventState = MutableSharedFlow<EventState>()
    val eventState = _eventState.asSharedFlow()


    fun createEvent(name: String) {
        viewModelScope.launch {
            _eventState.emit(EventState.Loading)
            val request = CreateEventRequest(name)
            val response = safeResponse { api.createEvent(request) }

            if (response is Resource.Success) {
                _eventState.emit(EventState.Success(response.data.id, response.data.joinCode, true))
            } else {
                _eventState.emit(EventState.Error)
            }
        }
    }

    fun joinEvent(code: String) {
        viewModelScope.launch {
            _eventState.emit(EventState.Loading)
            val request = JoinEventRequest(code)
            val response = safeResponse { api.joinEvent(request) }

            if (response is Resource.Success) {
                _eventState.emit(EventState.Success(response.data.id, response.data.joinCode, false))
            } else {
                _eventState.emit(EventState.InvalidCode)
            }
        }
    }

    sealed class EventState {
        data class Success(val id: String, val joincode: String, val isOwner: Boolean) : EventState()
        object Error : EventState()
        object InvalidCode : EventState()
        object Loading : EventState()
    }
}