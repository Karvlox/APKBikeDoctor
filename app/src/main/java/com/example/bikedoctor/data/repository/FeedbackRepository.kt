package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.MetricsResponse
import com.example.bikedoctor.data.remote.FeedbackApi
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class FeedbackRepository {
    private val feedbackApi = RetrofitClient.feedbackApi

    fun getFeedbackMetrics(): Call<MetricsResponse> {
        return feedbackApi.getFeedbackMetrics()
    }

    fun getReasonsMetricsByPhrase(): Call<MetricsResponse> {
        return feedbackApi.getReasonsMetricsByPhrase()
    }

    fun getReasonsMetricsByWord(): Call<MetricsResponse> {
        return feedbackApi.getReasonsMetricsByWord()
    }

    fun getDeliveryMetrics(): Call<MetricsResponse> {
        return feedbackApi.getDeliveryMetrics()
    }
}