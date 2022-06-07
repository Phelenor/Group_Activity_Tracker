package com.rafaelboban.groupactivitytracker.data.model

data class ParticipantData(
    val id: String,
    val name: String,
    val lastUpdateTimestamp: Long,
    val status: Status,
) {

    enum class Status(val text: String) {
        WAITING("Waiting"), ACTIVE("Active"), FINISHED("Finished"), LEFT("Left")
    }
}
