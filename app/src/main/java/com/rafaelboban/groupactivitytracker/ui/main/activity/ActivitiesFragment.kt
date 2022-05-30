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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.FragmentActivityBinding
import com.rafaelboban.groupactivitytracker.ui.main.MainActivityViewModel
import com.rafaelboban.groupactivitytracker.ui.main.activity.adapter.EventAdapter
import com.rafaelboban.groupactivitytracker.ui.main.activity.dialog.TYPE_CREATE_EVENT
import com.rafaelboban.groupactivitytracker.ui.main.activity.dialog.TYPE_JOIN_EVENT
import com.rafaelboban.groupactivitytracker.ui.main.activity.dialog.TYPE_RESUME_EVENT
import com.rafaelboban.groupactivitytracker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ActivitiesFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding: FragmentActivityBinding get() = _binding!!

    private val viewModel by viewModels<ActivitiesViewModel>()
    private val activityViewModel by activityViewModels<MainActivityViewModel>()

    private val adapter by lazy {
        EventAdapter(requireContext()).apply {
            setOnListClickListener { _, _, item ->
                findNavController().navigate(ActivitiesFragmentDirections.actionActivityFragmentToEventDetailsActivity(item))
            }
        }
    }

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

        setupRecyclerView()
        setupPullToRefresh()
        setupListeners()
        setupObservers()

        if (activityViewModel.isActivityListInitialized.not()) {
            activityViewModel.isActivityListInitialized = true
            viewModel.getActivities()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onStart() {
        super.onStart()
        val eventId = preferences.getString(Constants.PREFERENCE_EVENT_ID, "")!!
        viewModel.isEventActive(eventId)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityListState.collect { state ->
                    when (state) {
                        is ActivitiesViewModel.ActivityListState.Success -> {
                            binding.emptyState.isVisible = false
                            binding.swipeRefreshLayout.isRefreshing = false
                            adapter.updateItems(state.events)
                        }
                        is ActivitiesViewModel.ActivityListState.Empty -> {
                            binding.swipeRefreshLayout.isRefreshing = false
                            binding.emptyState.isVisible = true
                        }
                        is ActivitiesViewModel.ActivityListState.Loading -> {
                            binding.swipeRefreshLayout.isRefreshing = true
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityViewModel.eventState.collect { state ->
                    when (state) {
                        is MainActivityViewModel.EventState.InvalidCode -> {
                            Snackbar.make(requireView(), R.string.invalid_join_code, Snackbar.LENGTH_LONG).show()
                        }
                        is MainActivityViewModel.EventState.Error -> {
                            Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventStatusState.collect { state ->
                    when (state) {
                        is ActivitiesViewModel.EventStatus.Active -> showResumeButton()
                        is ActivitiesViewModel.EventStatus.Inactive -> hideResumeButton()
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun showResumeButton() {
        binding.run {
            buttonStartActivity.isVisible = false
            buttonJoinActivity.isVisible = false
            buttonResumeActivity.isVisible = true
        }
    }

    private fun hideResumeButton() {
        binding.run {
            buttonStartActivity.isVisible = true
            buttonJoinActivity.isVisible = true
            buttonResumeActivity.isVisible = false
        }
    }

    private fun setupListeners() {
        binding.buttonJoinActivity.setOnClickListener {
            buttonClickType = TYPE_JOIN_EVENT
            checkLocationPermission()
        }

        binding.buttonStartActivity.setOnClickListener {
            buttonClickType = TYPE_CREATE_EVENT
            checkLocationPermission()
        }

        binding.buttonResumeActivity.setOnClickListener {
            buttonClickType = TYPE_RESUME_EVENT
            checkLocationPermission()
        }
    }

    private fun performClickAction() {
        when (buttonClickType) {
            TYPE_CREATE_EVENT ->
                findNavController().navigate(
                    ActivitiesFragmentDirections.actionActivityFragmentToEventBottomSheet(TYPE_CREATE_EVENT)
                )
            TYPE_JOIN_EVENT ->
                findNavController().navigate(
                    ActivitiesFragmentDirections.actionActivityFragmentToEventBottomSheet(TYPE_JOIN_EVENT)
                )
            TYPE_RESUME_EVENT -> {
                val eventId = preferences.getString(Constants.PREFERENCE_EVENT_ID, null) ?: return
                val joincode = preferences.getString(Constants.PREFERENCE_JOINCODE, "")!!
                val isOwner = preferences.getBoolean(Constants.PREFERENCE_IS_OWNER, false)
                findNavController().navigate(
                    ActivitiesFragmentDirections.actionActivityFragmentToEventActivity(
                        eventId,
                        joincode,
                        isOwner
                    )
                )
            }
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
                Snackbar.make(requireView(), R.string.location_permission_required, Snackbar.LENGTH_INDEFINITE).run {
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
            setOnRefreshListener {
                viewModel.getActivities()
                preferences.getString(Constants.PREFERENCE_EVENT_ID, null)?.let { id ->
                    viewModel.isEventActive(id)
                }
            }
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