package com.rafaelboban.groupactivitytracker.ui.main.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rafaelboban.groupactivitytracker.databinding.FragmentHistoryBinding
import com.rafaelboban.groupactivitytracker.databinding.FragmentProfileBinding
import com.rafaelboban.groupactivitytracker.ui.auth.login.LoginFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!

    private val viewModel by viewModels<ProfileViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.text.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToAuthenticationActivity())
            requireActivity().finish()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}