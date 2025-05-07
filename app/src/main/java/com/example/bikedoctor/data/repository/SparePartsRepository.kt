package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.SpareParts
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class SparePartsRepository {
    fun getSpareParts(pageNumber: Int, pageSize: Int): Call<List<SpareParts>> {
        return RetrofitClient.sparePartsAPI.getSpareParts(pageNumber, pageSize)
    }
}