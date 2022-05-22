package com.rafaelboban.groupactivitytracker.utils

import android.content.SharedPreferences
import androidx.core.content.edit

fun SharedPreferences.storeUserData(userId: String, username: String, email: String) {
    edit {
        putString(Constants.PREFERENCE_USER_ID, userId)
        putString(Constants.PREFERENCE_USERNAME, username)
        putString(Constants.PREFERENCE_EMAIL, email)
    }
}

fun SharedPreferences.removeUserData() {
    edit {
        remove(Constants.PREFERENCE_USER_ID)
        remove(Constants.PREFERENCE_USERNAME)
        remove(Constants.PREFERENCE_EMAIL)
    }
}

fun SharedPreferences.storeToken(jwtToken: String) {
    edit {
        putString(Constants.PREFERENCE_JWT_TOKEN, jwtToken)
    }
}

fun SharedPreferences.removeToken() {
    edit {
        remove(Constants.PREFERENCE_JWT_TOKEN)
    }
}