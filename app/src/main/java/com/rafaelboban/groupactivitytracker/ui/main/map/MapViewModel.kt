package com.rafaelboban.groupactivitytracker.ui.main.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    fun getMarkers() {

    }

    var firstMapLoad = true
}