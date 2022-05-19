package com.rafaelboban.groupactivitytracker.ui.main.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.network.ApiService
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val api: ApiService,
) : ViewModel() {

    private val _markersState = Channel<MarkersState>()
    val markersState = _markersState.receiveAsFlow()

    var firstMapLoad = true
    var firstMarkerLoad = true

    fun getMarkers() {
        viewModelScope.launch {
            _markersState.send(MarkersState.Loading)
            val response = safeResponse { api.getMarkers() }
            if (response is Resource.Success) {
                _markersState.send(MarkersState.Success(response.data))
            } else {
                _markersState.send(MarkersState.Error)
            }
        }
    }

    sealed class MarkersState {
        data class Success(val data: List<Marker>) : MarkersState()
        object Error : MarkersState()
        object Loading : MarkersState()
    }
}