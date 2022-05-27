package com.rafaelboban.groupactivitytracker.ui.main.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.model.EventData
import com.rafaelboban.groupactivitytracker.data.request.EventStatusRequest
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.ui.main.activity.ActivitiesViewModel.ActivityListState.*
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    val apiService: ApiService,
) : ViewModel() {

    private val _activityListState = MutableStateFlow<ActivityListState>(Empty)
    val activityListState: StateFlow<ActivityListState> = _activityListState

    private val _eventStatusState = MutableStateFlow<EventStatus>(EventStatus.Default)
    val eventStatusState: StateFlow<EventStatus> = _eventStatusState

    fun getActivities() {
        viewModelScope.launch {
            _activityListState.emit(Loading)
            val response = safeResponse { apiService.getEvents() }

            if (response is Resource.Success) {
                _activityListState.emit(Success(response.data))
            } else {
                _activityListState.emit(Empty)
            }
        }
    }

    fun isEventActive(eventId: String) {
        viewModelScope.launch {
            _eventStatusState.emit(EventStatus.Checking)
            val request = EventStatusRequest(eventId)
            val response = safeResponse { apiService.eventStatus(request) }

            if (response is Resource.Success) {
                _eventStatusState.emit(EventStatus.Active)
            } else {
                _eventStatusState.emit(EventStatus.Inactive)
            }
        }
    }

    sealed class ActivityListState {
        data class Success(val events: List<EventData>) : ActivityListState()
        object Empty : ActivityListState()
        object Loading : ActivityListState()
    }

    sealed class EventStatus {
        object Active : EventStatus()
        object Inactive : EventStatus()
        object Checking : EventStatus()
        object Default : EventStatus()
    }
}