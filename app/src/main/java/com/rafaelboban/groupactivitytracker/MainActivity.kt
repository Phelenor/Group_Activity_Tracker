package com.rafaelboban.groupactivitytracker

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.Display
import android.view.WindowInsetsController
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.rafaelboban.groupactivitytracker.databinding.ActivityMainBinding
import com.rafaelboban.groupactivitytracker.utils.DisplayHelper
import com.rafaelboban.groupactivitytracker.utils.LocationHelper
import com.rafaelboban.groupactivitytracker.utils.PermissionHelper
import com.rafaelboban.groupactivitytracker.utils.hideKeyboard
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("PotentialBehaviorOverride")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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

        checkForLocationPermission()

        lifecycleScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(45.81319195859056, 15.976980944279237), 10f))
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
        val hasPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
        if (hasPermission.not()) {
            PermissionHelper.requestPermission(this, ACCESS_FINE_LOCATION) {
                googleMap.isMyLocationEnabled = true
            }
        } else {
            googleMap.isMyLocationEnabled = true
        }
    }
}