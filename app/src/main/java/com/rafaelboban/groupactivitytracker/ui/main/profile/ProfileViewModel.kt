package com.rafaelboban.groupactivitytracker.ui.main.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.network.ApiService
import com.rafaelboban.groupactivitytracker.ui.auth.login.LoginViewModel
import com.rafaelboban.groupactivitytracker.ui.main.map.MapViewModel
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.removeUserData
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferences: SharedPreferences,
    private val api: ApiService,
) : ViewModel() {

    private val _markersState = Channel<MarkersState>()
    val markersState = _markersState.receiveAsFlow()


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

    fun logout() {
        preferences.removeUserData()
    }

    sealed class MarkersState {
        data class Success(val data: List<Marker>) : MarkersState()
        object Error : MarkersState()
        object Loading : MarkersState()
    }
}