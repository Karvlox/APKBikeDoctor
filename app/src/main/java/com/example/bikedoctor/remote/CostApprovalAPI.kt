package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.CostApproval
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CostApprovalAPI {
    @GET("api/CostApproval")
    fun getCostApproval(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<CostApproval>>
}