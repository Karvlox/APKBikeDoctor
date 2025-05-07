package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.model.Motorcycle
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MotorcycleApi {
    @POST("api/Motorcycle")
    fun createMotorcycle(@Body motorcycle: Motorcycle): Call<Motorcycle>

    @GET("api/Motorcycle")
    fun getMotorcycles(): Call<List<Motorcycle>>
}