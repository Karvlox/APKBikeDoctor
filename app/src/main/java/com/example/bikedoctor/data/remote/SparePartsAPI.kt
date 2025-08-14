package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.ReceptionPost
import com.example.bikedoctor.data.model.SpareParts
import com.example.bikedoctor.data.model.SparePartsPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SparePartsAPI {
    @GET("api/SpareParts")
    fun getSpareParts(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<SpareParts>>

    @POST("api/SpareParts")
    fun createSpareParts(@Body sparePartsPost: SparePartsPost): Call<SparePartsPost>

    @PUT("api/SpareParts/{id}")
    fun updateSpareParts(
        @Path("id") id: String,
        @Body sparePartsPost: SparePartsPost
    ): Call<SparePartsPost>

    @PATCH("api/SpareParts/{id}/reviewed")
    fun updateReviewedStatus(
        @Path("id") id: String,
        @Query("reviewed") reviewed: Boolean
    ): Call<Void>

    @GET("api/SpareParts/by-employee/{ci}")
    fun getSparePartsByEmployee(
        @Path("ci") ci: Int,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<SpareParts>>
}