package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.data.model.Event
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_PHASE_CHANGE

data class PhaseChange(
    var phase: Event.Phase,
    val eventId: String,
) : BaseModel(TYPE_PHASE_CHANGE)