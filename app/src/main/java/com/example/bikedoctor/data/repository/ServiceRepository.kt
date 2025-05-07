package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Service
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class ServiceRepository {
    fun createService(service: Service): Call<Service> {
        return RetrofitClient.serviceApi.createService(service)
    }
}