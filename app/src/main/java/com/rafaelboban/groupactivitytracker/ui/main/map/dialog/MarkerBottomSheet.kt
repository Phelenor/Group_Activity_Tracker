package com.rafaelboban.groupactivitytracker.ui.main.map.dialog

import android.os.Bundle
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
import com.rafaelboban.groupactivitytracker.ui.main.MainActivityViewModel
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.MarkerBottomSheetLayoutBinding
import com.rafaelboban.groupactivitytracker.utils.LocationHelper
import kotlinx.coroutines.launch

class MarkerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: MarkerBottomSheetLayoutBinding

    private val viewModel by activityViewModels<MainActivityViewModel>()

    private val args by navArgs<MarkerBottomSheetArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MarkerBottomSheetLayoutBinding.inflate(inflater, container, false)

        val latLng = args.latLng ?: LatLng(args.marker?.latitude ?: 0.0, args.marker?.longitude ?: 0.0)
        val latLngFormatted = LocationHelper.formatLatLng(latLng)
        binding.latLng.text = latLngFormatted

        args.latLng?.let {
            binding.buttonCreate.text = getString(R.string.create)
            binding.buttonDelete.isVisible = false
            binding.tilTitle.hint = requireContext().getString(R.string.title)
            binding.tilDescription.hint = requireContext().getString(R.string.description)
        }

        args.marker?.let { marker ->
            binding.buttonCreate.text = getString(R.string.update)
            binding.buttonDelete.isVisible = true
            binding.title.isVisible = false
            binding.etTitle.hint = marker.title
            binding.tilTitle.hint = ""
            binding.etDescription.hint = marker.snippet
            binding.tilDescription.hint = ""
        }

        setupListeners()
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createState.collect { state ->
                    when (state) {
                        is MainActivityViewModel.MarkerCreateState.Loading -> {
                            binding.progressIndicator.isVisible = true
                        }
                        else -> dismiss()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteState.collect { state ->
                    when (state) {
                        is MainActivityViewModel.MarkerDeleteState.Loading -> {
                            binding.progressIndicator.isVisible = true
                        }
                        else -> dismiss()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonCreate.setOnClickListener {
            args.latLng?.let { createClick() }
            args.marker?.let { updateClick() }
        }

        binding.buttonDelete.setOnClickListener {
            args.marker?.let { deleteClick() }
        }
    }

    private fun createClick() {
        val latLng = args.latLng!!
        val latLngFormatted = LocationHelper.formatLatLng(latLng)
        val description = binding.etDescription.text.toString().trim().takeIf { it.isNotBlank() }
        val title = if (binding.etTitle.text.toString().trim().isNotBlank()) {
            binding.etTitle.text.toString()
        } else {
            latLngFormatted
        }

        viewModel.createMarker(title, description, latLng.latitude, latLng.longitude)
    }

    private fun updateClick() {
        val marker = args.marker!!
        val description = binding.etDescription.text.toString().trim().takeIf { it.isNotBlank() } ?: marker.snippet
        val title = binding.etTitle.text.toString().trim().takeIf { it.isNotBlank() } ?: marker.title
        viewModel.createMarker(title, description, marker.latitude, marker.longitude, marker.id)
    }

    private fun deleteClick() {
        val marker = args.marker!!
        viewModel.deleteMarker(marker.id)
    }
}