package com.rafaelboban.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.ButtCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.ktx.addPolyline
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.FragmentMapsBinding
import com.rafaelboban.groupactivitytracker.services.TrackerService
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.Constants.ACTION_SERVICE_START
import com.rafaelboban.groupactivitytracker.utils.PermissionHelper
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapsFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private var locationList = mutableListOf<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMapsBinding.inflate(layoutInflater)

        locationClient = FusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        binding.buttonStart.setOnClickListener {
            if (PermissionHelper.hasBackgroundLocationPermission(requireContext())) {
                sendActionCommandToService(ACTION_SERVICE_START)
                binding.buttonStart.isVisible = false
                binding.buttonEnd.isVisible = true
            } else {
                PermissionHelper.requestBackgroundLocationPermission(this)
            }
        }

        binding.buttonEnd.setOnClickListener {
            stopForegroundService()
            binding.buttonEnd.isVisible = false
            binding.buttonStart.isVisible = true
        }

        lifecycleScope.launchWhenCreated {
            googleMap = mapFragment.awaitMap()

            if (checkForLocationPermission()) {
                locationClient.lastLocation.addOnCompleteListener {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.result.latitude, it.result.longitude), 14f))
                }
            }

            observeTrackerService()
            googleMap.awaitMapLoad()

            googleMap.uiSettings.apply {
                isZoomControlsEnabled = false
                isZoomGesturesEnabled = false
                isTiltGesturesEnabled = false
                isMyLocationButtonEnabled = true
                isCompassEnabled = false
                isRotateGesturesEnabled = false
            }

            googleMap.setMinZoomPreference(10f)
            googleMap.setMaxZoomPreference(18f)
        }

        return binding.root
    }

    private fun observeTrackerService() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TrackerService.locationList.collect {
                    if (it.isNotEmpty()) {
                        binding.buttonStart.isVisible = false
                        binding.buttonEnd.isVisible = true
                        binding.buttonEnd.isEnabled = true
                    }
                    locationList = it
                    drawRouteAndPosition()
                    followPolyLine()
                }
            }
        }
    }

    private fun drawRouteAndPosition() {
        googleMap.addPolyline {
            width(10f)
            color(Color.CYAN)
            jointType(JointType.ROUND)
            startCap(ButtCap())
            endCap(RoundCap())
            addAll(locationList)
        }
    }

    private fun followPolyLine() {
        if (locationList.isNotEmpty()) {
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(locationList.last(), 15f)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun checkForLocationPermission(): Boolean {
        if (PermissionHelper.hasLocationPermission(requireContext())) {
            googleMap.isMyLocationEnabled = true
        } else {
            PermissionHelper.requestLocationPermission(this)
        }
        return googleMap.isMyLocationEnabled
    }

    private fun sendActionCommandToService(action: String) {
        Intent(requireContext(), TrackerService::class.java).run {
            this.action = action
            requireContext().startService(this)
        }
    }

    private fun stopForegroundService() {
        sendActionCommandToService(Constants.ACTION_SERVICE_STOP)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireContext()).build().show()
        } else {
            PermissionHelper.requestLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        googleMap.isMyLocationEnabled = true
    }
}