package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Repair
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class RepairRepository {
    fun getRepairs(pageNumber: Int, pageSize: Int): Call<List<Repair>> {
        return RetrofitClient.repairAPI.getRepair(pageNumber, pageSize)
    }
}