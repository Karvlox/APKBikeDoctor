package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.model.ReceptionSend
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class ReceptionRepository {
    fun getReceptions(pageNumber: Int, pageSize: Int): Call<List<Reception>> {
        return RetrofitClient.receptionApi.getReceptions(pageNumber, pageSize)
    }

    fun createReception(reception: ReceptionSend): Call<ReceptionSend> {
        return RetrofitClient.receptionApi.createReception(reception)
    }
}