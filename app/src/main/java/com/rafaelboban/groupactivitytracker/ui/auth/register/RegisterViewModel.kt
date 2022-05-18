package com.rafaelboban.groupactivitytracker.ui.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelboban.groupactivitytracker.data.request.RegisterRequest
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
class RegisterViewModel @Inject constructor(private val authApi: AuthApi) : ViewModel() {

    private val _registerChannel = Channel<RegisterState>()
    val registerChannel = _registerChannel.receiveAsFlow()

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _registerChannel.send(RegisterState.Loading)

            val request = RegisterRequest(username, email, password)
            val response = safeResponse { authApi.register(request) }

            if (response is Resource.Success) {
                if (response.data.isSuccessful) {
                    _registerChannel.send(RegisterState.Success)
                } else {
                    when {
                        response.data.message.contains("email", true) -> {
                            _registerChannel.send(RegisterState.EmailTaken)
                        }
                        response.data.message.contains("username", true) -> {
                            _registerChannel.send(RegisterState.UsernameTaken)
                        }
                    }
                }
            } else {
                _registerChannel.send(RegisterState.Failure)
            }
        }
    }

    fun isUsernameValid(username: String) = username.length >= 3

    fun isEmailValid(email: String) = email.matches(Patterns.EMAIL_ADDRESS.toRegex())

    fun isPasswordValid(password: String) = password.matches(Constants.PASSWORD_REGEX_PATTERN.toRegex())

    fun isPasswordConfirmed(password: String, confirmPassword: String) = password == confirmPassword

    sealed class RegisterState() {
        object Success : RegisterState()
        object Failure : RegisterState()
        object EmailTaken : RegisterState()
        object UsernameTaken : RegisterState()
        object Loading : RegisterState()
    }
}