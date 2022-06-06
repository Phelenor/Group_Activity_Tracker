package com.rafaelboban.groupactivitytracker.ui.main.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.rafaelboban.groupactivitytracker.utils.removeToken
import com.rafaelboban.groupactivitytracker.utils.removeUserData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferences: SharedPreferences,
) : ViewModel() {

    fun logout() {
        preferences.removeUserData()
        preferences.removeToken()
    }
}