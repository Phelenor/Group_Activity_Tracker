package com.rafaelboban.groupactivitytracker.ui.auth.login

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.request.LoginRequest
import com.rafaelboban.groupactivitytracker.di.AppModule
import com.rafaelboban.groupactivitytracker.network.api.ApiService
import com.rafaelboban.groupactivitytracker.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val api: ApiService,
    @AppModule.PreferencesStandard private val sharedPreferences: SharedPreferences,
    @AppModule.PreferencesEncrypted private val encryptedPreferences: SharedPreferences,
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
            val response = executeRequest { api.login(request) }

            if (response is Resource.Success) {
                val token = response.data.token
                encryptedPreferences.storeToken(token)
                authenticate()
            } else {
                _loginChannel.send(LoginState.Failure)
            }
        }
    }

    private fun authenticate() {
        encryptedPreferences.getString(Constants.PREFERENCE_JWT_TOKEN, null) ?: return
        viewModelScope.launch {
            _loginChannel.send(LoginState.Loading)

            val response = executeRequest { api.authenticate() }
            if (response is Resource.Success) {
                val user = response.data
                sharedPreferences.storeUserData(user.userId, user.username, user.email)
                _loginChannel.send(LoginState.Success)
            } else {
                sharedPreferences.removeUserData()
                encryptedPreferences.removeToken()
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