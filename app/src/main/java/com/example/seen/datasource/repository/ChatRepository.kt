package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.chat.Conversation
import com.example.seen.domain.model.chatbot.AskChatbotRequest
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User

class ChatRepository {

    suspend fun getConversations(token: String) =
        RetrofitInstance.api.getConversations(token)

    suspend fun getConversationDetails(token: String, conversationId: Int) =
        RetrofitInstance.api.getConversationDetails(token, conversationId)

    suspend fun searchChatFriends(token: String, query: String) =
        RetrofitInstance.api.searchChatFriends(token, query)

}