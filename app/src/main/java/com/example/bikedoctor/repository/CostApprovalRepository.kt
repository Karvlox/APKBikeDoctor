package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class CostApprovalRepository {
    fun getCostApprovals(pageNumber: Int, pageSize: Int): Call<List<CostApproval>> {
        return RetrofitClient.costApprovalAPI.getCostApproval(pageNumber, pageSize)
    }
}