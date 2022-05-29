package com.rafaelboban.groupactivitytracker.ui.auth.register

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding: FragmentRegisterBinding get() = _binding!!

    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        setupFocusListeners()
        setupTextWatcher()
        setupOnClickListeners()
        setupObservers()

        return binding.root
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerChannel.collect { state ->
                    when (state) {
                        RegisterViewModel.RegisterState.Success -> {
                            val action = RegisterFragmentDirections.actionRegisterToLogin().apply {
                                registeredFlag = true
                            }
                            findNavController().navigate(action)
                        }
                        RegisterViewModel.RegisterState.EmailTaken -> {
                            binding.progressIndicator.isVisible = false
                            Snackbar.make(requireView(), R.string.email_taken, Snackbar.LENGTH_LONG).show()
                        }
                        RegisterViewModel.RegisterState.UsernameTaken -> {
                            binding.progressIndicator.isVisible = false
                            Snackbar.make(requireView(), R.string.username_taken, Snackbar.LENGTH_LONG).show()
                        }
                        RegisterViewModel.RegisterState.Failure -> {
                            binding.progressIndicator.isVisible = false
                            Snackbar.make(requireView(), R.string.unknown_error, Snackbar.LENGTH_LONG).show()
                        }
                        RegisterViewModel.RegisterState.Loading -> {
                            binding.progressIndicator.isVisible = true
                        }
                    }
                }
            }
        }
    }

    private fun setupOnClickListeners() {
        binding.buttonRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.register(username, email, password)
        }
    }

    private fun setupTextWatcher() {
        val validationWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                val username = binding.etUsername.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val confirmPassword = binding.etPasswordConfirm.text.toString().trim()
                binding.buttonRegister.isEnabled =
                    viewModel.isUsernameValid(username) && viewModel.isEmailValid(email) &&
                            viewModel.isPasswordValid(password) && viewModel.isPasswordConfirmed(password, confirmPassword)


            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        }

        val usernameWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (viewModel.isUsernameValid(binding.etUsername.text?.trim().toString())) {
                    binding.tilUsername.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        }

        val emailWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (viewModel.isEmailValid(binding.etEmail.text?.trim().toString())) {
                    binding.tilEmail.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        }

        val passwordWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (viewModel.isPasswordValid(binding.etPassword.text?.trim().toString())) {
                    binding.tilPassword.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        }

        val confirmPasswordWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                binding.tilPasswordConfirm.error = null
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        }

        binding.etUsername.addTextChangedListener(validationWatcher)
        binding.etUsername.addTextChangedListener(usernameWatcher)
        binding.etEmail.addTextChangedListener(validationWatcher)
        binding.etEmail.addTextChangedListener(emailWatcher)
        binding.etPassword.addTextChangedListener(validationWatcher)
        binding.etPassword.addTextChangedListener(passwordWatcher)
        binding.etPasswordConfirm.addTextChangedListener(validationWatcher)
        binding.etPasswordConfirm.addTextChangedListener(confirmPasswordWatcher)
    }

    private fun setupFocusListeners() {
        binding.etUsername.setOnFocusChangeListener { _, isFocused ->
            if (isFocused.not()) {
                if (viewModel.isUsernameValid(binding.etUsername.text.toString().trim()).not()) {
                    binding.tilUsername.error = getString(R.string.username_minimum_length)
                } else {
                    binding.tilUsername.error = null
                }
            }
        }

        binding.etEmail.setOnFocusChangeListener { _, isFocused ->
            if (isFocused.not()) {
                if (viewModel.isEmailValid(binding.etEmail.text.toString().trim()).not()) {
                    binding.tilEmail.error = getString(R.string.use_valid_email)
                } else {
                    binding.tilEmail.error = null
                }

            }
        }

        binding.etPassword.setOnFocusChangeListener { _, isFocused ->
            if (isFocused.not()) {
                if (viewModel.isPasswordValid(binding.etPassword.text.toString().trim()).not()) {
                    binding.tilPassword.error = getString(R.string.password_minimum_length)
                } else {
                    binding.tilPassword.error = null
                }
            }
        }

        binding.etPasswordConfirm.setOnFocusChangeListener { _, isFocused ->
            if (isFocused.not()) {
                val password = binding.etPassword.text.toString().trim()
                val confirmPassword = binding.etPasswordConfirm.text.toString().trim()
                if (viewModel.isPasswordConfirmed(password, confirmPassword).not()) {
                    binding.tilPasswordConfirm.error = getString(R.string.passwords_dont_match)
                } else {
                    binding.tilPasswordConfirm.error = null
                }
            }
        }

        binding.etPasswordConfirm.setOnEditorActionListener { view, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                view.clearFocus()
                true
            }
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}