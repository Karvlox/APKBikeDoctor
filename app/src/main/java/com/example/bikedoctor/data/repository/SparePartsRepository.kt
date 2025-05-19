package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.ReceptionPost
import com.example.bikedoctor.data.model.SpareParts
import com.example.bikedoctor.data.model.SparePartsPost
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class SparePartsRepository {
    fun getSpareParts(pageNumber: Int, pageSize: Int): Call<List<SpareParts>> {
        return RetrofitClient.sparePartsAPI.getSpareParts(pageNumber, pageSize)
    }

    fun createSpareParts(sparePartsPost: SparePartsPost): Call<SparePartsPost> {
        return RetrofitClient.sparePartsAPI.createSpareParts(sparePartsPost)
    }

    fun updateSpareParts(id: String, sparePartsPost: SparePartsPost): Call<SparePartsPost> {
        return RetrofitClient.sparePartsAPI.updateSpareParts(id, sparePartsPost)
    }

    fun updateReviewedStatus(id: String, reviewed: Boolean): Call<Void> {
        return RetrofitClient.sparePartsAPI.updateReviewedStatus(id, reviewed)
    }
}