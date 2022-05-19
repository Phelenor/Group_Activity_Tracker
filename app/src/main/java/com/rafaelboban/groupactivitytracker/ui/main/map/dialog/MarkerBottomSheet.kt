package com.rafaelboban.groupactivitytracker.ui.main.map.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rafaelboban.groupactivitytracker.MainActivityViewModel
import com.rafaelboban.groupactivitytracker.databinding.MarkerBottomSheetLayoutBinding
import com.rafaelboban.groupactivitytracker.utils.LocationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MarkerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: MarkerBottomSheetLayoutBinding

    private val viewModel by activityViewModels<MainActivityViewModel>()

    private val args by navArgs<MarkerBottomSheetArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MarkerBottomSheetLayoutBinding.inflate(inflater, container, false)

        val latLng = args.latLng
        val latLngFormatted = LocationHelper.formatLatLng(latLng)
        binding.latLng.text = latLngFormatted

        setupListeners(latLng, latLngFormatted)
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.markerCreateState.collect { state ->
                    when (state) {
                        is MainActivityViewModel.MarkerCreateState.Loading -> {
                            binding.progressIndicator.isVisible = true
                        }
                        is MainActivityViewModel.MarkerCreateState.Finished -> {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun setupListeners(latLng: LatLng, latLngFormatted: String) {
        binding.buttonCreate.setOnClickListener {
            val description = binding.etDescription.text.toString().trim().takeIf { it.isNotBlank() }
            val title = if (binding.etTitle.text.toString().trim().isNotBlank()) {
                binding.etTitle.text.toString()
            } else {
                latLngFormatted
            }

            viewModel.createMarker(title, description, latLng.latitude, latLng.longitude)
        }
    }
}