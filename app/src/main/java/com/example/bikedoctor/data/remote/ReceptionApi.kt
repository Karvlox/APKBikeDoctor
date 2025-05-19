package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.model.ReceptionPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ReceptionApi {
    @GET("api/Reception")
    fun getReceptions(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Reception>>

    @POST("api/Reception")
    fun createReception(@Body reception: ReceptionPost): Call<ReceptionPost>

    @PUT("api/Reception/{id}")
    fun updateReception(
        @Path("id") id: String,
        @Body reception: ReceptionPost
    ): Call<ReceptionPost>

    @PATCH("api/Reception/{id}/reviewed")
    fun updateReviewedStatus(
        @Path("id") id: String,
        @Query("reviewed") reviewed: Boolean
    ): Call<Void>
}