package com.rafaelboban.groupactivitytracker.ui.main.profile

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.rafaelboban.groupactivitytracker.di.AppModule
import com.rafaelboban.groupactivitytracker.utils.removeToken
import com.rafaelboban.groupactivitytracker.utils.removeUserData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @AppModule.PreferencesStandard private val sharedPreferences: SharedPreferences,
    @AppModule.PreferencesEncrypted private val encryptedPreferences: SharedPreferences,
) : ViewModel() {

    fun logout() {
        sharedPreferences.removeUserData()
        encryptedPreferences.removeToken()
    }
}