package com.example.bikedoctor.data.remote

import com.example.bikedoctor.data.model.MetricsResponse
import retrofit2.Call
import retrofit2.http.GET

interface FeedbackApi {
    @GET("api/Feedback/metrics")
    fun getFeedbackMetrics(): Call<MetricsResponse>

    @GET("api/Reception/metrics/reasons/phrases")
    fun getReasonsMetricsByPhrase(): Call<MetricsResponse>

    @GET("api/Reception/metrics/reasons/words")
    fun getReasonsMetricsByWord(): Call<MetricsResponse>

    @GET("api/Delivery/metrics")
    fun getDeliveryMetrics(): Call<MetricsResponse>
}