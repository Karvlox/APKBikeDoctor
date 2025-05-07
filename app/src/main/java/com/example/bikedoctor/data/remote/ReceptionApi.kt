package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Reception
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ReceptionApi {
    @GET("api/Reception")
    fun getReceptions(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Reception>>
}