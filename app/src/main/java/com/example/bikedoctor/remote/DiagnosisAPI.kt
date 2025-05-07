package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Diagnosis
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface DiagnosisAPI {
    @GET("api/Diagnosis")
    fun getDiagnosis(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Diagnosis>>
}