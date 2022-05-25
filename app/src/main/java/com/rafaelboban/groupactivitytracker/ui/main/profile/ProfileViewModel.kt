package com.rafaelboban.groupactivitytracker.ui.main.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.removeToken
import com.rafaelboban.groupactivitytracker.utils.removeUserData
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
        preferences.removeToken()
    }

    sealed class MarkersState {
        data class Success(val data: List<Marker>) : MarkersState()
        object Error : MarkersState()
        object Loading : MarkersState()
    }
}