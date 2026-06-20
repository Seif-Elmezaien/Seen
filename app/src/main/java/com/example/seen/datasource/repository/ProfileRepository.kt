package com.example.seen.datasource.repository

import com.example.seen.datasource.remote.RetrofitInstance
import com.example.seen.domain.model.community.PostUser
import com.example.seen.domain.model.profile.response.ProfileResponse
import retrofit2.Response

class ProfileRepository {

    suspend fun getUserProfile(token: String, userId: Int): Response<ProfileResponse> =
        RetrofitInstance.api.getUserProfile(token, userId)

    suspend fun sendFriendRequest(token: String, userId: Int) =
        RetrofitInstance.api.sendFriendRequest(token, userId)

    suspend fun acceptFriendRequest(token: String, userId: Int) =
        RetrofitInstance.api.acceptFriendRequest(token, userId)

    suspend fun cancelFriendRequest(token: String, userId: Int) =
        RetrofitInstance.api.cancelFriendRequest(token, userId)

    suspend fun removeFriend(token: String, userId: Int) =
        RetrofitInstance.api.removeFriend(token, userId)

    suspend fun blockFriend(token: String, userId: Int) =
        RetrofitInstance.api.blockFriend(token, userId)

    suspend fun unblockFriend(token: String, userId: Int) =
        RetrofitInstance.api.unblockFriend(token, userId)

    suspend fun getBlockedUsers(token: String): Response<List<PostUser>> =
        RetrofitInstance.api.getBlockedUsers(token)

    suspend fun getFriendsProfile(token: String): Response<List<PostUser>> =
        RetrofitInstance.api.getFriendsProfile(token)

}