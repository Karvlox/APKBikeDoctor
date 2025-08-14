package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class MessageNotificationRepository {
    fun sendNotification(messageNotification: MessageNotification): Call<Void> {
        return RetrofitClient.messageNotificationAPI.sendMessage(messageNotification)
    }
}