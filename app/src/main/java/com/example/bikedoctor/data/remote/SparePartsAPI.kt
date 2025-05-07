package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.SpareParts
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SparePartsAPI {
    @GET("api/SpareParts")
    fun getSpareParts(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<SpareParts>>
}