package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.MessageNotification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MessageNotificationAPI {
    @POST("api/Send/Send")
    fun sendMessage(@Body notification: MessageNotification): Call<Void>
}