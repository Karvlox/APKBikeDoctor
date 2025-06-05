package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.Diagnosis
import com.example.bikedoctor.data.model.DiagnosisPost
import com.example.bikedoctor.data.model.ReceptionPost
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DiagnosisAPI {
    @GET("api/Diagnosis")
    fun getDiagnosis(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int
    ): Call<List<Diagnosis>>

    @POST("api/Diagnosis")
    fun createDiagnosis(@Body diagnosis: DiagnosisPost): Call<DiagnosisPost>

    @PUT("api/Diagnosis/{id}")
    fun updateDiagnosis(
        @Path("id") id: String,
        @Body diagnosisPost: DiagnosisPost
    ): Call<DiagnosisPost>

    @PATCH("api/Diagnosis/{id}/reviewed")
    fun updateReviewedStatus(
        @Path("id") id: String,
        @Query("reviewed") reviewed: Boolean
    ): Call<Void>
}