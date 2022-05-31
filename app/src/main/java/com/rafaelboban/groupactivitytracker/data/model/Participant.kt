package com.rafaelboban.groupactivitytracker.data.model

data class Participant(
    val id: String,
    val name: String,
    val lastUpdateTimestamp: Long,
    val status: ParticipantStatus,
)

enum class ParticipantStatus(val text: String) {
    ACTIVE("Active"), FINISHED("Finished"), LEFT("Left")
}