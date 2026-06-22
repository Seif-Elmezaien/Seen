package com.example.seen.datasource.repository

import com.example.seen.datasource.local.SeenDatabase
import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.chat.Conversation
import com.example.seen.domain.model.chat.SendMessageResponse
import com.example.seen.domain.model.chatbot.AskChatbotRequest
import com.example.seen.domain.model.entites.Log
import com.example.seen.domain.model.entites.RecordGlucose
import com.example.seen.domain.model.entites.RecordMeal
import com.example.seen.domain.model.entites.RecordMedication
import com.example.seen.domain.model.entites.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ChatRepository {

    suspend fun getConversations(token: String) =
        RetrofitInstance.api.getConversations(token)

    suspend fun getConversationDetails(token: String, conversationId: Int) =
        RetrofitInstance.api.getConversationDetails(token, conversationId)

    suspend fun searchChatFriends(token: String, query: String) =
        RetrofitInstance.api.searchChatFriends(token, query)

    suspend fun getMessages(token: String, receiverId: Int, page: Int = 1) =
        RetrofitInstance.api.getMessages(token, receiverId, page)

    suspend fun sendMessage(
        token: String,
        receiverId: Int,
        text: String?,
        imagePart: MultipartBody.Part? = null,
        voicePart: MultipartBody.Part? = null,
        videoPart: MultipartBody.Part? = null
    ): Response<SendMessageResponse> {
        val receiverBody = receiverId.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val messageBody = text?.toRequestBody("text/plain".toMediaTypeOrNull())
        return RetrofitInstance.api.sendMessage(token, receiverBody, messageBody, imagePart, voicePart, videoPart)
    }

    suspend fun editMessage(token: String, messageId: Int, newText: String) =
        RetrofitInstance.api.editMessage(token, messageId, mapOf("message" to newText))

    suspend fun deleteMessage(token: String, messageId: Int) =
        RetrofitInstance.api.deleteMessage(token, messageId)

    suspend fun markAsRead(token: String, conversationId: Int) =
        RetrofitInstance.api.markChatAsRead(token, conversationId)
}