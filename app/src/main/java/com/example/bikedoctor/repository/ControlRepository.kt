package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.QualityControl
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class ControlRepository {
    fun getControls(pageNumber: Int, pageSize: Int): Call<List<QualityControl>> {
        return RetrofitClient.controlAPI.getControl(pageNumber, pageSize)
    }
}