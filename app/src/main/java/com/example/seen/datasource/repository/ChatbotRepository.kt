package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.chatbot.AskChatbotRequest
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User

class ChatbotRepository {

    suspend fun askChatbot(
        token: String,
        message: String
    ) = RetrofitInstance.api.askChatbot(
        token,
        AskChatbotRequest(message)
    )

    suspend fun getChatbotHistory(
        token: String
    ) = RetrofitInstance.api.getChatbotHistory(
        token
    )
}