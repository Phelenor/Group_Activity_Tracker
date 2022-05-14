package com.rafaelboban.groupactivitytracker.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

object PermissionHelper {

    fun requestPermission(context: Context, permission: String, isGrantedCallback: (() -> Unit)? = null) {
        (context as? ComponentActivity)?.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isGrantedCallback?.invoke()
            }
        }?.launch(permission)
    }
}