package com.rafaelboban.groupactivitytracker

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.rafaelboban.groupactivitytracker.databinding.ActivityMainBinding
import com.rafaelboban.groupactivitytracker.utils.DisplayHelper
import com.rafaelboban.groupactivitytracker.utils.LocationHelper
import com.rafaelboban.groupactivitytracker.utils.PermissionHelper
import com.rafaelboban.groupactivitytracker.utils.hideKeyboard
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@SuppressLint("PotentialBehaviorOverride")
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                val latLng = LocationHelper.getLatLngFromQuery(this, binding.inputEditText.text.toString())
                latLng?.let {
                    lifecycleScope.launch {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                    }
                    true
                }
            }
            false
        }

        lifecycleScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(45.81319195859056, 15.976980944279237), 10f))

            checkForLocationPermission()

            googleMap.awaitMapLoad()

            googleMap.uiSettings.apply {
                isZoomControlsEnabled = true
                isTiltGesturesEnabled = true
                isMyLocationButtonEnabled = true
            }

            googleMap.setPadding(0, DisplayHelper.convertDpToPx(this@MainActivity, 64), 0, 0)

            googleMap.setMinZoomPreference(10f)
            googleMap.setMaxZoomPreference(18f)

            googleMap.addMarker {
                position(LatLng(45.81319195859056, 15.976980944279237))
                title("Trg bana Jelačića")
            }
        }
    }

    private fun checkForLocationPermission() {
        if (PermissionHelper.hasLocationPermission(this)) {
            googleMap.isMyLocationEnabled = true
        } else {
            PermissionHelper.requestLocationPermission(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            PermissionHelper.requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        googleMap.isMyLocationEnabled = true
    }
}