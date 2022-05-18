package com.rafaelboban.groupactivitytracker.ui.auth.login

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.request.LoginRequest
import com.rafaelboban.groupactivitytracker.network.AuthApi
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.Resource
import com.rafaelboban.groupactivitytracker.utils.safeResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authApi: AuthApi,
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
            val response = safeResponse { authApi.login(request) }

            if (response is Resource.Success) {
                val token = response.data.token
                preferences.edit {
                    putString(Constants.PREFERENCE_JWT_TOKEN, token)
                }
                _loginChannel.send(LoginState.Success)
            } else {
                _loginChannel.send(LoginState.Failure)
            }
        }
    }

    private fun authenticate() {
        preferences.getString(Constants.PREFERENCE_JWT_TOKEN, null) ?: return
        viewModelScope.launch {
            _loginChannel.send(LoginState.Loading)

            val response = safeResponse { authApi.authenticate() }
            if (response is Resource.Success) {
                val user = response.data
                preferences.edit {
                    putString(Constants.PREFERENCE_USER_ID, user.userId)
                    putString(Constants.PREFERENCE_USERNAME, user.username)
                    putString(Constants.PREFERENCE_EMAIL, user.email)
                }
                _loginChannel.send(LoginState.Success)
            } else {
                preferences.edit {
                    remove(Constants.PREFERENCE_USER_ID)
                    remove(Constants.PREFERENCE_USERNAME)
                    remove(Constants.PREFERENCE_EMAIL)
                }
                _loginChannel.send(LoginState.TokenExpired)
            }
        }
    }

    sealed class LoginState() {
        object Success : LoginState()
        object Failure : LoginState()
        object TokenExpired : LoginState()
        object Loading : LoginState()
    }
}