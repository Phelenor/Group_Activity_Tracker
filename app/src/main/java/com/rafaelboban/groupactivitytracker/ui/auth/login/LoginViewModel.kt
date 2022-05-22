package com.rafaelboban.groupactivitytracker.ui.auth.login

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.request.LoginRequest
import com.rafaelboban.groupactivitytracker.network.ApiService
import com.rafaelboban.groupactivitytracker.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: ApiService,
    private val preferences: SharedPreferences,
) : ViewModel() {

    private val _loginChannel = Channel<LoginState>()
    val loginChannel = _loginChannel.receiveAsFlow()

    init {
        authenticate()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginChannel.send(LoginState.Loading)

            val request = LoginRequest(email, password)
            val response = safeResponse { api.login(request) }

            if (response is Resource.Success) {
                val token = response.data.token
                preferences.storeToken(token)
                authenticate()
            } else {
                _loginChannel.send(LoginState.Failure)
            }
        }
    }

    private fun authenticate() {
        preferences.getString(Constants.PREFERENCE_JWT_TOKEN, null) ?: return
        viewModelScope.launch {
            _loginChannel.send(LoginState.Loading)

            val response = safeResponse { api.authenticate() }
            if (response is Resource.Success) {
                val user = response.data
                preferences.storeUserData(user.userId, user.username, user.email)
                _loginChannel.send(LoginState.Success)
            } else {
                preferences.removeUserData()
                preferences.removeToken()
                _loginChannel.send(LoginState.TokenExpired)
            }
        }
    }

    fun isEmailValid(email: String) = email.isNotBlank()

    fun isPasswordValid(password: String) = password.isNotBlank()

    sealed class LoginState {
        object Success : LoginState()
        object Failure : LoginState()
        object TokenExpired : LoginState()
        object Loading : LoginState()
    }
}