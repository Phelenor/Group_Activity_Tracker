package com.rafaelboban.groupactivitytracker.data.model

data class Event(val name: String, val ownerId: String) {

    enum class Phase {
        WAITING,
        IN_PROGRESS,
        FINISHED
    }
}