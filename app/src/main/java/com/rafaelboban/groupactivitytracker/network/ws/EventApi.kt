package com.rafaelboban.groupactivitytracker.network.ws

import com.rafaelboban.groupactivitytracker.data.socket.BaseModel
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow

interface EventApi {

    @Receive
    fun observeEvents(): Flow<WebSocket.Event>

    @Send
    fun sendBaseModel(baseModel: BaseModel): Boolean

    @Receive
    fun observeBaseModels(): Flow<BaseModel>
}