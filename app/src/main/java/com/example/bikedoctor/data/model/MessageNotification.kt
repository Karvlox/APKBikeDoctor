package com.example.bikedoctor.data.model

import com.google.gson.annotations.SerializedName

data class MessageNotification(
    @SerializedName("Texto") val message: String
)