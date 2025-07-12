package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.SparePartsPost
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class CostApprovalRepository {
    fun getCostApprovals(pageNumber: Int, pageSize: Int): Call<List<CostApproval>> {
        return RetrofitClient.costApprovalAPI.getCostApproval(pageNumber, pageSize)
    }

    fun getCostApprovalsByEmployee(ci: Int, pageNumber: Int, pageSize: Int): Call<List<CostApproval>> {
        return RetrofitClient.costApprovalAPI.getCostApprovalsByEmployee(ci, pageNumber, pageSize)
    }

    fun createCostApprovals(costApprovalPost: CostApprovalPost): Call<CostApprovalPost> {
        return RetrofitClient.costApprovalAPI.createCostApproval(costApprovalPost)
    }

    fun updateCostApprovals(id: String, costApprovalPost: CostApprovalPost): Call<CostApprovalPost> {
        return RetrofitClient.costApprovalAPI.updateCostApproval(id, costApprovalPost)
    }

    fun updateReviewedStatus(id: String, reviewed: Boolean): Call<Void> {
        return RetrofitClient.costApprovalAPI.updateReviewedStatus(id, reviewed)
    }
}