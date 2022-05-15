package com.rafaelboban.groupactivitytracker.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
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
}