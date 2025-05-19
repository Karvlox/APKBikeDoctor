package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.model.ReceptionPost
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class ReceptionRepository {
    fun getReceptions(pageNumber: Int, pageSize: Int): Call<List<Reception>> {
        return RetrofitClient.receptionApi.getReceptions(pageNumber, pageSize)
    }

    fun createReception(reception: ReceptionPost): Call<ReceptionPost> {
        return RetrofitClient.receptionApi.createReception(reception)
    }

    fun updateReception(id: String, reception: ReceptionPost): Call<ReceptionPost> {
        return RetrofitClient.receptionApi.updateReception(id, reception)
    }

    fun updateReviewedStatus(id: String, reviewed: Boolean): Call<Void> {
        return RetrofitClient.receptionApi.updateReviewedStatus(id, reviewed)
    }
}