package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class DeliveryRepository {
    fun getDelivery(pageNumber: Int, pageSize: Int): Call<List<Delivery>> {
        return RetrofitClient.deliveryAPI.getDelivery(pageNumber, pageSize)
    }
}