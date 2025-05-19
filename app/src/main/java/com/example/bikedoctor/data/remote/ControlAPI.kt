package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.QualityControl
import com.example.bikedoctor.data.model.QualityControlPost
import com.example.bikedoctor.data.model.RepairPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ControlAPI {
    @GET("api/QualityControl")
    fun getControl(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<QualityControl>>

    @POST("api/QualityControl")
    fun createQualityControl(@Body qualityControlPost: QualityControlPost): Call<QualityControlPost>

    @PUT("api/QualityControl/{id}")
    fun updateQualityControl(
        @Path("id") id: String,
        @Body qualityControlPost: QualityControlPost
    ): Call<QualityControlPost>

    @PATCH("api/QualityControl/{id}/reviewed")
    fun updateReviewedStatus(
        @Path("id") id: String,
        @Query("reviewed") reviewed: Boolean
    ): Call<Void>
}