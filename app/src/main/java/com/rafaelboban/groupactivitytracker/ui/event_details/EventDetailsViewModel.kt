package com.rafaelboban.groupactivitytracker.ui.event_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.executeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    val apiService: ApiService,
) : ViewModel() {

    private val _eventPointsState = MutableStateFlow<PointsState>(PointsState.Default)
    val eventPointsState: StateFlow<PointsState> = _eventPointsState

    private val _eventMarkersState = MutableStateFlow<MarkersState>(MarkersState.Default)
    val eventMarkersState: StateFlow<MarkersState> = _eventMarkersState

    fun getPoints(eventId: String) {
        viewModelScope.launch {
            _eventPointsState.emit(PointsState.Loading)

            val response = executeRequest { apiService.getPoints(eventId) }

            if (response is Resource.Success) {
                if (response.data.isNotEmpty()) {
                    _eventPointsState.emit(PointsState.Success(response.data))
                } else {
                    _eventPointsState.emit(PointsState.Empty)
                }
            } else {
                _eventPointsState.emit(PointsState.Error)
            }
        }
    }

    fun getMarkers(eventId: String) {
        viewModelScope.launch {
            val response = executeRequest { apiService.getMarkers(eventId) }

            if (response is Resource.Success) {
                _eventMarkersState.emit(MarkersState.Success(response.data))
            } else {
                _eventMarkersState.emit(MarkersState.Error)
            }
        }
    }

    sealed class PointsState {
        data class Success(val data: List<LatLng>) : PointsState()
        object Empty : PointsState()
        object Error : PointsState()
        object Loading : PointsState()
        object Default : PointsState()
    }

    sealed class MarkersState {
        data class Success(val data: List<Marker>) : MarkersState()
        object Error : MarkersState()
        object Default : MarkersState()
    }
}