package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Delivery
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DeliveryAPI {
    @GET("api/Delivery")
    fun getDelivery(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Delivery>>
}