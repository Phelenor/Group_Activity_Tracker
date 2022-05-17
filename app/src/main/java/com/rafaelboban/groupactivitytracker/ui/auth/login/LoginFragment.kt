package com.rafaelboban.groupactivitytracker.ui.auth.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rafaelboban.groupactivitytracker.databinding.FragmentLoginBinding
import com.rafaelboban.groupactivitytracker.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

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

        return binding.root
    }

    private fun setupOnClickListeners() {
        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginToRegister())
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
                    binding.tilEmail.error = "Please use a valid email address."
                } else {
                    binding.tilEmail.error = null
                }

            }
        }

        binding.etPassword.setOnFocusChangeListener { _, isFocused ->
            if (isFocused.not()) {
                if (isPasswordValid().not()) {
                    binding.tilPassword.error = "Minimum length: 8\nInclude both letters and numbers."
                } else {
                    binding.tilPassword.error = null
                }
            }
        }
    }

    private fun isEmailValid(): Boolean {
        val emailText = binding.etEmail.text.toString()
        return emailText.matches(Patterns.EMAIL_ADDRESS.toRegex())
    }

    private fun isPasswordValid(): Boolean {
        val passwordText = binding.etPassword.text.toString()
        return passwordText.matches(Constants.PASSWORD_REGEX_PATTERN.toRegex())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
