package com.rafaelboban.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rafaelboban.groupactivitytracker.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint


@SuppressLint("PotentialBehaviorOverride")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}