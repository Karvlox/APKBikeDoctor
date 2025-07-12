package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.Repair
import com.example.bikedoctor.data.model.RepairPost
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class RepairRepository {
    fun getRepairs(pageNumber: Int, pageSize: Int): Call<List<Repair>> {
        return RetrofitClient.repairAPI.getRepair(pageNumber, pageSize)
    }

    fun getRepairsByEmployee(ci: Int, pageNumber: Int, pageSize: Int): Call<List<Repair>> {
        return RetrofitClient.repairAPI.getRepairsByEmployee(ci, pageNumber, pageSize)
    }

    fun createRepairs(repairPost: RepairPost): Call<RepairPost> {
        return RetrofitClient.repairAPI.createRepair(repairPost)
    }

    fun updateRepairs(id: String, repairPost: RepairPost): Call<RepairPost> {
        return RetrofitClient.repairAPI.updateRepair(id, repairPost)
    }

    fun updateReviewedStatus(id: String, reviewed: Boolean): Call<Void> {
        return RetrofitClient.repairAPI.updateReviewedStatus(id, reviewed)
    }
}