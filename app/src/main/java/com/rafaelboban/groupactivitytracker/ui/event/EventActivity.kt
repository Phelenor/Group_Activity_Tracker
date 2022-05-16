package com.rafaelboban.groupactivitytracker.ui.event

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.rafaelboban.groupactivitytracker.databinding.ActivityEventBinding
import com.rafaelboban.groupactivitytracker.ui.auth.AuthenticationViewModel

class EventActivity : AppCompatActivity() {

    private val binding by lazy { ActivityEventBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<EventViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}