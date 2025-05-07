package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Repair
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RepairAPI {
    @GET("api/Repair")
    fun getRepair(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Repair>>
}