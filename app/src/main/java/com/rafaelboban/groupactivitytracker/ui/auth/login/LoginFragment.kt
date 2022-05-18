package com.rafaelboban.groupactivitytracker.ui.auth.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rafaelboban.groupactivitytracker.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding get() = _binding!!

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        setupFocusListeners()
        setupTextWatcher()
        setupOnClickListeners()
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginChannel.collect { state ->
                    when (state) {
                        LoginViewModel.LoginState.Success -> {
                            findNavController().navigate(LoginFragmentDirections.actionLoginToMain())
                            requireActivity().finish()
                        }
                        LoginViewModel.LoginState.Failure -> {
                            binding.progressIndicator.isVisible = false
                            Snackbar.make(requireView(), "Wrong email or password.", Snackbar.LENGTH_LONG).show()
                        }
                        LoginViewModel.LoginState.TokenExpired -> {
                            binding.progressIndicator.isVisible = false
                            Snackbar.make(requireView(), "Session expired.", Snackbar.LENGTH_LONG).show()
                        }
                        LoginViewModel.LoginState.Loading -> {
                            binding.progressIndicator.isVisible = true
                        }
                    }
                }
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginToRegister())
        }

        binding.buttonLogin.setOnClickListener {
            viewModel.login(binding.etEmail.text.toString(), binding.etPassword.text.toString())
        }
    }

    private fun setupTextWatcher() {
        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                binding.buttonLogin.isEnabled = isEmailValid() && isPasswordValid()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        }

        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
    }

    private fun setupFocusListeners() {
        binding.etEmail.setOnFocusChangeListener { _, isFocused ->
            if (isFocused.not()) {
                if (isEmailValid().not()) {
                    binding.tilEmail.error = "Please enter an email address."
                } else {
                    binding.tilEmail.error = null
                }

            }
        }

        binding.etPassword.setOnFocusChangeListener { _, isFocused ->
            if (isFocused.not()) {
                if (isPasswordValid().not()) {
                    binding.tilPassword.error = "Please enter a password."
                } else {
                    binding.tilPassword.error = null
                }
            }
        }
    }

    private fun isEmailValid(): Boolean {
        val emailText = binding.etEmail.text.toString()
        return emailText.isNotBlank()
    }

    private fun isPasswordValid(): Boolean {
        val passwordText = binding.etPassword.text.toString()
        return passwordText.isNotBlank()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
