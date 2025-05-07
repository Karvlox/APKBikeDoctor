package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.model.Motorcycle
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class MotorcycleRepository {
    fun createMotorcycle(motorcycle: Motorcycle): Call<Motorcycle> {
        return RetrofitClient.motorcycleApi.createMotorcycle(motorcycle)
    }

    fun getMotorcycles(): Call<List<Motorcycle>> {
        return RetrofitClient.motorcycleApi.getMotorcycles()
    }
}