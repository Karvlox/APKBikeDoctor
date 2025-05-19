package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.ReceptionPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CostApprovalAPI {
    @GET("api/CostApproval")
    fun getCostApproval(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<CostApproval>>

    @POST("api/CostApproval")
    fun createCostApproval(@Body costApprovalPost: CostApprovalPost): Call<CostApprovalPost>

    @PUT("api/CostApproval/{id}")
    fun updateCostApproval(
        @Path("id") id: String,
        @Body costApprovalPost: CostApprovalPost
    ): Call<CostApprovalPost>

    @PATCH("api/CostApproval/{id}/reviewed")
    fun updateReviewedStatus(
        @Path("id") id: String,
        @Query("reviewed") reviewed: Boolean
    ): Call<Void>
}