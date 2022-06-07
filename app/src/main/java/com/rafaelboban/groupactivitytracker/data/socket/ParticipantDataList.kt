package com.rafaelboban.groupactivitytracker.data.socket

import com.rafaelboban.groupactivitytracker.data.model.ParticipantData
import com.rafaelboban.groupactivitytracker.utils.Constants.TYPE_USER_STATUS


data class ParticipantDataList(
    val list: List<ParticipantData>,
) : BaseModel(TYPE_USER_STATUS)
