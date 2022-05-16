package com.rafaelboban.groupactivitytracker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.rafaelboban.groupactivitytracker.utils.Constants.PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE
import com.rafaelboban.groupactivitytracker.utils.Constants.PERMISSION_LOCATION_REQUEST_CODE
import com.vmadalin.easypermissions.EasyPermissions

object PermissionHelper {

    fun hasLocationPermission(context: Context) =
        EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_FINE_LOCATION)

    fun requestLocationPermission(fragment: Fragment) {
        EasyPermissions.requestPermissions(
            fragment,
            "This application cannot work without location permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun requestLocationPermission(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            "This application cannot work without location permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun hasBackgroundLocationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return EasyPermissions.hasPermissions(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return true
    }

    fun requestBackgroundLocationPermission(fragment: Fragment) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return EasyPermissions.requestPermissions(
                fragment,
                "Background location permission needed.",
                PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }
}