package com.rafaelboban.groupactivitytracker.ui.main.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.ui.main.activity.ActivityViewModel.ActivityListState.*
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    val apiService: ApiService,
) : ViewModel() {

    private val _activityListState = MutableStateFlow<ActivityListState>(Empty)
    val activityListState: StateFlow<ActivityListState> = _activityListState

    fun getActivities() {
        viewModelScope.launch {
            _activityListState.emit(Loading)
            val response = safeResponse { apiService.getActivities() }

            if (response is Resource.Success) {
                _activityListState.emit(Success(response.data))
            } else {
                _activityListState.emit(Empty)
            }
        }
    }

    sealed class ActivityListState {
        data class Success(val activities: List<String>) : ActivityListState()
        object Empty : ActivityListState()
        object Loading : ActivityListState()
    }
}