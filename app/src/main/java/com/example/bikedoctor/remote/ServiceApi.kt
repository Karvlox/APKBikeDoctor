package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Service
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ServiceApi {
    @POST("api/Service")
    fun createService(@Body service: Service): Call<Service>
}