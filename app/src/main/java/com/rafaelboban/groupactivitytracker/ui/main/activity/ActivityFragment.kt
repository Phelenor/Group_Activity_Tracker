package com.rafaelboban.groupactivitytracker.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rafaelboban.groupactivitytracker.databinding.FragmentActivityBinding
import com.rafaelboban.groupactivitytracker.services.TrackerService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding: FragmentActivityBinding get() = _binding!!

    private val viewModel by viewModels<ActivityViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)

        setupListeners()

        return binding.root
    }

    private fun setupListeners() {
        binding.buttonStartActivity.setOnClickListener {
            findNavController().navigate(ActivityFragmentDirections.actionActivityFragmentToEventActivity())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}