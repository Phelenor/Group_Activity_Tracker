package com.rafaelboban.groupactivitytracker.ui.main.profile

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.data.model.Marker
import com.rafaelboban.groupactivitytracker.databinding.FragmentProfileBinding
import com.rafaelboban.groupactivitytracker.utils.KMLHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!

    private val viewModel by viewModels<ProfileViewModel>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.getMarkers()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupListeners()
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.markersState.collect { state ->
                    when (state) {
                        is ProfileViewModel.MarkersState.Loading -> {
                            binding.progressIndicator.isVisible = true
                        }
                        is ProfileViewModel.MarkersState.Success -> {
                            binding.progressIndicator.isVisible = false
                            try {
                                exportMarkersToKML(state.data)
                                Snackbar.make(requireView(), "File saved in Documents directory.", Snackbar.LENGTH_LONG).show()
                            } catch (e: IOException) {
                                Snackbar.make(requireView(), "Error while exporting file.", Snackbar.LENGTH_LONG).show()
                            }
                        }
                        is ProfileViewModel.MarkersState.Error -> {
                            binding.progressIndicator.isVisible = false
                            Snackbar.make(requireView(), "Error loading markers.", Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToAuthenticationActivity())
            requireActivity().finish()
        }

        binding.buttonExportMarkers.setOnClickListener {
            checkLocationPermission()
        }
    }

    private fun exportMarkersToKML(markers: List<Marker>) {
        val string = KMLHelper.exportMarkersToKML(markers)

        val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "markers.kml")
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            val extVolumeUri: Uri = MediaStore.Files.getContentUri("external")
            val fileUri = requireContext().contentResolver.insert(extVolumeUri, values)
            requireContext().contentResolver.openOutputStream(fileUri!!)
        } else {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString()
            val file = File(path, "markers.kml")
            FileOutputStream(file)
        }

        val bytes: ByteArray = string.toByteArray()
        outputStream?.write(bytes)
        outputStream?.close()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> viewModel.getMarkers()
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                Snackbar.make(requireView(), "Storage permission required.", Snackbar.LENGTH_INDEFINITE).run {
                    setAction(getString(R.string.ok)) {
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }.show()
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}