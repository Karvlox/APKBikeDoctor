package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.Repair
import com.example.bikedoctor.data.model.RepairPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RepairAPI {
    @GET("api/Repair")
    fun getRepair(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Repair>>

    @POST("api/Repair")
    fun createRepair(@Body repairPost: RepairPost): Call<RepairPost>

    @PUT("api/Repair/{id}")
    fun updateRepair(
        @Path("id") id: String,
        @Body repairPost: RepairPost
    ): Call<RepairPost>

    @PATCH("api/Repair/{id}/reviewed")
    fun updateReviewedStatus(
        @Path("id") id: String,
        @Query("reviewed") reviewed: Boolean
    ): Call<Void>

    @GET("api/Repair/by-employee/{ci}")
    fun getRepairsByEmployee(
        @Path("ci") ci: Int,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Repair>>
}