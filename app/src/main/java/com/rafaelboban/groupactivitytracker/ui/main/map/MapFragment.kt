package com.rafaelboban.groupactivitytracker.ui.main.map

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.rafaelboban.groupactivitytracker.MainActivityViewModel
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.FragmentMapBinding
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.DisplayHelper
import com.rafaelboban.groupactivitytracker.utils.LocationHelper
import com.rafaelboban.groupactivitytracker.utils.VibrationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding: FragmentMapBinding get() = _binding!!

    private val viewModel by viewModels<MapViewModel>()
    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private lateinit var googleMap: GoogleMap

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var locationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                enableMyLocation()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            setupMap(mapFragment)
        }

        setupListeners()
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.markerChannel.collect { marker ->
                    if (marker != null) {
                        googleMap.addMarker {
                            position(LatLng(marker.latitude, marker.longitude))
                            title(marker.title)
                            marker.snippet?.let { snippet(it) }
                        }
                    } else {
                        Snackbar.make(requireView(), "Marker creation error.", Snackbar.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                lifecycleScope.launch {
                    searchForLocationByName(binding.etSearch.text.toString().trim())
                }
            }
            false
        }
    }

    private suspend fun setupMap(mapFragment: SupportMapFragment) {
        googleMap = mapFragment.awaitMap()

        checkLocationPermission()

        googleMap.setPadding(0, DisplayHelper.convertDpToPx(requireContext(), 56), 0, 0)
        googleMap.uiSettings.run {
            isZoomControlsEnabled = true
            isMapToolbarEnabled = true
        }

        googleMap.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {

            override fun onMarkerDragEnd(marker: Marker) {
                Log.d("MARIN", "onMarkerDragEnd: $marker")
            }

            override fun onMarkerDrag(marker: Marker) = Unit

            override fun onMarkerDragStart(marker: Marker) = Unit
        })

        googleMap.awaitMapLoad()
        showMarkerUsageTooltip()

        googleMap.setOnMapLongClickListener { latLng ->
            VibrationHelper.vibrateAddMarket(requireContext())
            findNavController().navigate(MapFragmentDirections.actionMapToBottomSheet(latLng))
        }
    }

    private fun showMarkerUsageTooltip() {
        val showMarkerTooltip = preferences.getBoolean(Constants.PREFERENCE_MARKER_TOOLTIP_SHOWN, false).not()
        if (showMarkerTooltip) {
            preferences.edit { putBoolean(Constants.PREFERENCE_MARKER_TOOLTIP_SHOWN, true) }
            Snackbar.make(requireView(), "Long press on map to add a marker.", Snackbar.LENGTH_INDEFINITE).run {
                setAction(getString(R.string.dismiss)) {
                    dismiss()
                }
            }.show()
        }
    }

    private fun searchForLocationByName(locationName: String) {
        val latLng = LocationHelper.getLatLngFromQuery(requireContext(), locationName)
        if (latLng == null) {
            Snackbar.make(requireView(), "Location not found.", Snackbar.LENGTH_LONG).show()
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    private fun enableMyLocation() {
        googleMap.isMyLocationEnabled = true
        if (viewModel.firstMapLoad) {
            viewModel.firstMapLoad = false
            locationClient.lastLocation.addOnCompleteListener {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.result.latitude, it.result.longitude), 13f))
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> enableMyLocation()
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                Snackbar.make(requireView(), "Location permission required.", Snackbar.LENGTH_INDEFINITE).run {
                    setAction(getString(R.string.ok)) {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }.show()
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}