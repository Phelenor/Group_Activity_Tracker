package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.data.model.ParticipantData
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_PARTICIPANT_LIST

data class ParticipantList(
    val participantData: List<ParticipantData>,
) : BaseModel(TYPE_PARTICIPANT_LIST)