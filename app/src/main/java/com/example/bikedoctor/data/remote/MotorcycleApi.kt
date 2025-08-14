package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Motorcycle
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MotorcycleApi {
    @POST("api/Motorcycle")
    fun createMotorcycle(@Body motorcycle: Motorcycle): Call<Motorcycle>

    @GET("api/Motorcycle")
    fun getMotorcycles(): Call<List<Motorcycle>>

    @GET("api/Motorcycle/licensePlate/{licensePlate}")
    fun getMotorcycleByLicensePlate(@Path("licensePlate") licensePlate: String): Call<Motorcycle>

    @DELETE("api/Motorcycle/licensePlate/{licensePlate}")
    fun deleteMotorcycleByLicensePlate(@Path("licensePlate") licensePlate: String): Call<Void>

    @PUT("api/Motorcycle/licensePlate/{licensePlate}")
    fun updateMotorcycle(@Path("licensePlate") licensePlate: String, @Body motorcycle: Motorcycle): Call<Void>
}