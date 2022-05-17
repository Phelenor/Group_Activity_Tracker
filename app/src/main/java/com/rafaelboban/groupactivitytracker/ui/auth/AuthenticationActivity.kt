package com.rafaelboban.groupactivitytracker.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rafaelboban.groupactivitytracker.databinding.ActivityAuthenticationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticationActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAuthenticationBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}