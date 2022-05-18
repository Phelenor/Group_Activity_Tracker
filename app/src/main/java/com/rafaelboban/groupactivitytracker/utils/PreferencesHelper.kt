package com.rafaelboban.groupactivitytracker.utils

import android.content.SharedPreferences
import androidx.core.content.edit

fun SharedPreferences.removeUserData() {
    edit {
        remove(Constants.PREFERENCE_USER_ID)
        remove(Constants.PREFERENCE_USERNAME)
        remove(Constants.PREFERENCE_EMAIL)
        remove(Constants.PREFERENCE_JWT_TOKEN)
    }
}

fun SharedPreferences.storeUserData(userId: String, username: String, email: String) {
    edit {
        putString(Constants.PREFERENCE_USER_ID, userId)
        putString(Constants.PREFERENCE_USERNAME, username)
        putString(Constants.PREFERENCE_EMAIL, email)
    }
}