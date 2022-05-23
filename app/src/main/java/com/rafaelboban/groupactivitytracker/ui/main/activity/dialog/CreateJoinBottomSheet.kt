package com.rafaelboban.groupactivitytracker.ui.main.activity.dialog

import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rafaelboban.groupactivitytracker.ui.main.MainActivityViewModel
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.EventBottomSheetLayoutBinding
import com.rafaelboban.groupactivitytracker.databinding.MarkerBottomSheetLayoutBinding
import com.rafaelboban.groupactivitytracker.utils.Constants
import com.rafaelboban.groupactivitytracker.utils.LocationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


const val TYPE_CREATE_EVENT = 1
const val TYPE_JOIN_EVENT = 2

@AndroidEntryPoint
class CreateJoinBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: EventBottomSheetLayoutBinding

    private val viewModel by activityViewModels<MainActivityViewModel>()

    private val args by navArgs<CreateJoinBottomSheetArgs>()

    @Inject
    lateinit var preferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = EventBottomSheetLayoutBinding.inflate(inflater, container, false)

        when (args.type) {
            TYPE_CREATE_EVENT -> {
                val username = preferences.getString(Constants.PREFERENCE_USERNAME, "")
                val startTitle = requireContext().getString(R.string.activity_name_format, username)
                binding.etName.setText(startTitle)
            }
            TYPE_JOIN_EVENT -> {
                binding.title.text = requireContext().getString(R.string.join_activity)
                binding.tilName.hint = requireContext().getString(R.string.code)
                binding.buttonCreate.text = requireContext().getString(R.string.join)
                binding.buttonCreate.isEnabled = false
            }
        }

        setupListeners()
        setupObservers()
        setupTextWatcher()

        return binding.root
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {


            }
        }
    }

    private fun setupListeners() {
        binding.buttonCreate.setOnClickListener {
            findNavController().navigate(CreateJoinBottomSheetDirections.actionCreateJoinEventBottomSheetToEventActivity())
            dismiss()
        }
    }

    private fun setupTextWatcher() {
        binding.etName.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                binding.buttonCreate.isEnabled = s.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit
        })
    }
}