package com.rafaelboban.groupactivitytracker.ui.main

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.rafaelboban.groupactivitytracker.R
import com.rafaelboban.groupactivitytracker.databinding.ActivityMainBinding
import com.rafaelboban.groupactivitytracker.ui.main.history.HistoryFragment
import com.rafaelboban.groupactivitytracker.ui.main.login.LoginFragment
import com.rafaelboban.groupactivitytracker.ui.main.map.MapFragment
import com.rafaelboban.groupactivitytracker.ui.main.profile.ProfileFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_main) as NavHostFragment).navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.history_fragment, R.id.map_fragment, R.id.profile_fragment -> {
                    binding.bottomNav.isVisible = true
                }
                else -> binding.bottomNav.isVisible = false
            }
        }

        binding.bottomNav.setupWithNavController(navController)
    }
}