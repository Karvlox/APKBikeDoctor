package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.model.DeliveryPost
import com.example.bikedoctor.data.model.QualityControlPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DeliveryAPI {
    @GET("api/Delivery")
    fun getDelivery(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Delivery>>

    @POST("api/Delivery")
    fun createDelivery(@Body deliveryPost: DeliveryPost): Call<DeliveryPost>

    @PUT("api/Delivery/{id}")
    fun updateDelivery(
        @Path("id") id: String,
        @Body deliveryPost: DeliveryPost
    ): Call<DeliveryPost>

    @PATCH("api/Delivery/{id}/reviewed")
    fun updateReviewedStatus(
        @Path("id") id: String,
        @Query("reviewed") reviewed: Boolean
    ): Call<Void>
}