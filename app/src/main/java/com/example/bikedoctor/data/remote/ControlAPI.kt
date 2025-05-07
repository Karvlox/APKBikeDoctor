package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.QualityControl
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ControlAPI {
    @GET("api/QualityControl")
    fun getControl(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<QualityControl>>
}