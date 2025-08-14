package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.QualityControl
import com.example.bikedoctor.data.model.QualityControlPost
import com.example.bikedoctor.data.model.RepairPost
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class ControlRepository {
    fun getControls(pageNumber: Int, pageSize: Int): Call<List<QualityControl>> {
        return RetrofitClient.controlAPI.getControl(pageNumber, pageSize)
    }

    fun getControlsByEmployee(ci: Int, pageNumber: Int, pageSize: Int): Call<List<QualityControl>> {
        return RetrofitClient.controlAPI.getControlsByEmployee(ci, pageNumber, pageSize)
    }

    fun createControls(qualityControlPost: QualityControlPost): Call<QualityControlPost> {
        return RetrofitClient.controlAPI.createQualityControl(qualityControlPost)
    }

    fun updateControls(id: String, qualityControlPost: QualityControlPost): Call<QualityControlPost> {
        return RetrofitClient.controlAPI.updateQualityControl(id, qualityControlPost)
    }

    fun updateReviewedStatus(id: String, reviewed: Boolean): Call<Void> {
        return RetrofitClient.controlAPI.updateReviewedStatus(id, reviewed)
    }
}