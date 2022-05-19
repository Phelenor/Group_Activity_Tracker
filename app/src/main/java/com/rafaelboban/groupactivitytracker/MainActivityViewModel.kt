package com.rafaelboban.groupactivitytracker

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.data.request.MarkerRequest
import com.rafaelboban.groupactivitytracker.network.ApiService
import com.rafaelboban.groupactivitytracker.ui.auth.login.LoginViewModel
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.isSuccess
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    val api: ApiService,
    val preferences: SharedPreferences,
) : ViewModel() {

    private val _markerChannel = Channel<Marker?>()
    val markerChannel = _markerChannel.receiveAsFlow()

    private val _markerCreateState = Channel<MarkerCreateState>()
    val markerCreateState = _markerCreateState.receiveAsFlow()

    private val _createState = MutableSharedFlow<State>()
    val createState = _createState.asSharedFlow()

    fun createMarker(title: String, description: String?, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _markerCreateState.send(MarkerCreateState.Loading)
            val request = MarkerRequest(title, description, latitude, longitude)
            val response = safeResponse { api.createMarker(request) }
            _markerCreateState.send(MarkerCreateState.Finished)

            if (response is Resource.Success) {
                _markerChannel.send(response.data)
            } else {
                _markerChannel.send(null)
            }
        }
    }

    sealed class MarkerCreateState {
        object Finished : MarkerCreateState()
        object Loading : MarkerCreateState()
    }

    sealed class State {
        data class Success(val marker: Marker) : MarkerCreateState()
        object Error : MarkerCreateState()
        object Loading : MarkerCreateState()
    }
}