package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.model.DeliveryPost
import com.example.bikedoctor.data.model.QualityControlPost
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class DeliveryRepository {
    fun getDelivery(pageNumber: Int, pageSize: Int): Call<List<Delivery>> {
        return RetrofitClient.deliveryAPI.getDelivery(pageNumber, pageSize)
    }

    fun createDelivery(deliveryPost: DeliveryPost): Call<DeliveryPost> {
        return RetrofitClient.deliveryAPI.createDelivery(deliveryPost)
    }

    fun updateDelivery(id: String, deliveryPost: DeliveryPost): Call<DeliveryPost> {
        return RetrofitClient.deliveryAPI.updateDelivery(id, deliveryPost)
    }

    fun updateReviewedStatus(id: String, reviewed: Boolean): Call<Void> {
        return RetrofitClient.deliveryAPI.updateReviewedStatus(id, reviewed)
    }
}