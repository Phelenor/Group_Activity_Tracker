package com.rafaelboban.groupactivitytracker.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.request.RegisterRequest
import com.rafaelboban.groupactivitytracker.network.AuthApi
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApi: AuthApi,
) : ViewModel() {

    private val _registerState = Channel<RegisterState>()
    val registerState = _registerState.receiveAsFlow()

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            val request = RegisterRequest(username, email, password)
            val response = safeResponse {
                authApi.register(request)
            }
            if (response is Resource.Success) {
                if (response.data.isSuccessful) {
                    _registerState.send(RegisterState.Success)
                } else {
                    when {
                        response.data.message.contains("email", true) -> {
                            _registerState.send(RegisterState.EmailTaken)
                        }
                        response.data.message.contains("username", true) -> {
                            _registerState.send(RegisterState.UsernameTaken)
                        }
                    }
                }
            } else {
                _registerState.send(RegisterState.Failure)
            }
        }
    }

    sealed class RegisterState() {
        object Success : RegisterState()
        object Failure : RegisterState()
        object EmailTaken : RegisterState()
        object UsernameTaken : RegisterState()
    }
}