package com.rafaelboban.groupactivitytracker.ui.event_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.model.LocationPoint
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.safeResponse
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

    fun getPoints(eventId: String) {
        viewModelScope.launch {
            _eventPointsState.emit(PointsState.Loading)

            val response = safeResponse { apiService.getPoints(eventId) }

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

    sealed class PointsState {
        data class Success(val data: List<LocationPoint>) : PointsState()
        object Empty : PointsState()
        object Error : PointsState()
        object Loading : PointsState()
        object Default : PointsState()
    }
}