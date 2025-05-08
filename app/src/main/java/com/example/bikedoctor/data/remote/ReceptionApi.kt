package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.model.ReceptionSend
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReceptionApi {
    @GET("api/Reception")
    fun getReceptions(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Reception>>

    @POST("api/Reception")
    fun createReception(@Body reception: ReceptionSend): Call<ReceptionSend>
}