package com.rafaelboban.groupactivitytracker.ui.main.activity

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.FragmentActivityBinding
import com.rafaelboban.groupactivitytracker.ui.auth.login.LoginFragmentDirections
import com.rafaelboban.groupactivitytracker.ui.main.MainActivityViewModel
import com.rafaelboban.groupactivitytracker.ui.main.activity.dialog.TYPE_CREATE_EVENT
import com.rafaelboban.groupactivitytracker.ui.main.activity.dialog.TYPE_JOIN_EVENT
import com.rafaelboban.groupactivitytracker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding: FragmentActivityBinding get() = _binding!!

    private val viewModel by viewModels<ActivityViewModel>()
    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private var buttonClickType: Int? = null

    @Inject
    lateinit var preferences: SharedPreferences

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                performClickAction()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)

        setupPullToRefresh()
        setupObservers()

        if (activityViewModel.isActivityListInitialized.not()) {
            activityViewModel.isActivityListInitialized = true
            viewModel.getActivities()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupListeners()
        setupViews()
    }

    private fun setupViews() {
        val eventId = preferences.getString(Constants.PREFERENCE_EVENT_ID, null)
        eventId?.let {
            binding.buttonJoinActivity.isVisible = false
            binding.buttonStartActivity.text = "Resume Activity"
            binding.buttonStartActivity.setOnClickListener {
                val joincode = preferences.getString(Constants.PREFERENCE_JOINCODE, "")!!
                val isOwner = preferences.getBoolean(Constants.PREFERENCE_IS_OWNER, false)
                findNavController().navigate(
                    ActivityFragmentDirections.actionActivityFragmentToEventActivity(
                        eventId,
                        joincode,
                        isOwner
                    )
                )
            }
        } ?: run {
            binding.buttonJoinActivity.isVisible = true
            binding.buttonStartActivity.text = "Start Activity"
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityListState.collect { state ->
                    binding.swipeRefreshLayout.isRefreshing = false
                    when (state) {
                        is ActivityViewModel.ActivityListState.Success -> {
                            binding.progressIndicator.isVisible = false
                            binding.emptyState.isVisible = false
                        }
                        is ActivityViewModel.ActivityListState.Empty -> {
                            binding.progressIndicator.isVisible = false
                            binding.emptyState.isVisible = true
                        }
                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.eventState.collect { state ->
                    when (state) {
                        is MainActivityViewModel.EventState.InvalidCode -> {
                            Snackbar.make(requireView(), "Invalid join code.", Snackbar.LENGTH_LONG).show()
                        }
                        is MainActivityViewModel.EventState.Error -> {
                            Snackbar.make(requireView(), "Unknown error.", Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.buttonStartActivity.setOnClickListener {
            buttonClickType = TYPE_CREATE_EVENT
            checkLocationPermission()
        }

        binding.buttonJoinActivity.setOnClickListener {
            buttonClickType = TYPE_JOIN_EVENT
            checkLocationPermission()
        }
    }

    private fun performClickAction() {
        when (buttonClickType) {
            TYPE_CREATE_EVENT ->
                findNavController().navigate(ActivityFragmentDirections.actionActivityFragmentToEventBottomSheet(TYPE_CREATE_EVENT))
            TYPE_JOIN_EVENT ->
                findNavController().navigate(ActivityFragmentDirections.actionActivityFragmentToEventBottomSheet(TYPE_JOIN_EVENT))
            else -> Unit
        }
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> performClickAction()
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

    private fun setupPullToRefresh() {
        binding.swipeRefreshLayout.run {
            setOnRefreshListener { viewModel.getActivities() }
            setColorSchemeColors(
                requireContext().getColor(R.color.md_theme_light_primary),
                requireContext().getColor(R.color.md_theme_light_primaryInverse)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}