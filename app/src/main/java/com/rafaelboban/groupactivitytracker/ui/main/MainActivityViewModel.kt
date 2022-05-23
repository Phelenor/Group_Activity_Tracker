package com.rafaelboban.groupactivitytracker.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.data.request.DeleteMarkerRequest
import com.rafaelboban.groupactivitytracker.data.request.MarkerRequest
import com.rafaelboban.groupactivitytracker.network.ApiService
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

    private val _createState = MutableSharedFlow<MarkerCreateState>()
    val createState = _createState.asSharedFlow()

    private val _deleteState = MutableSharedFlow<MarkerDeleteState>()
    val deleteState = _deleteState.asSharedFlow()

    fun createMarker(title: String, description: String?, latitude: Double, longitude: Double, id: String? = null) {
        viewModelScope.launch {
            _createState.emit(MarkerCreateState.Loading)
            val request = MarkerRequest(title, description, latitude, longitude, id)
            val response = safeResponse { api.createMarker(request) }

            if (response is Resource.Success) {
                id?.let {
                    _createState.emit(MarkerCreateState.UpdateSuccess(response.data))
                } ?: run {
                    _createState.emit(MarkerCreateState.Success(response.data))
                }
            } else {
                _createState.emit(MarkerCreateState.Error)
            }
        }
    }

    fun deleteMarker(id: String) {
        viewModelScope.launch {
            _deleteState.emit(MarkerDeleteState.Loading)
            val request = DeleteMarkerRequest(id)
            val response = safeResponse { api.deleteMarker(request) }

            if (response is Resource.Success) {
                _deleteState.emit(MarkerDeleteState.Success(response.data.id))
            } else {
                _deleteState.emit(MarkerDeleteState.Error)
            }
        }
    }

    sealed class MarkerCreateState {
        data class Success(val marker: Marker) : MarkerCreateState()
        data class UpdateSuccess(val marker: Marker) : MarkerCreateState()
        object Error : MarkerCreateState()
        object Loading : MarkerCreateState()
    }

    sealed class MarkerDeleteState {
        data class Success(val id: String) : MarkerDeleteState()
        object Error : MarkerDeleteState()
        object Loading : MarkerDeleteState()
    }
}