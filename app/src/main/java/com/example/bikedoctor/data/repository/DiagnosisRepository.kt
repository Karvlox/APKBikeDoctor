package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Diagnosis
import com.example.bikedoctor.data.model.DiagnosisPost
import com.example.bikedoctor.data.model.ReceptionPost
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class DiagnosisRepository {
    fun getDiagnosis(pageNumber: Int, pageSize: Int): Call<List<Diagnosis>> {
        return RetrofitClient.diagnosisAPI.getDiagnosis(pageNumber, pageSize)
    }

    fun createDiagnosis(diagnosis: DiagnosisPost): Call<DiagnosisPost> {
        return RetrofitClient.diagnosisAPI.createDiagnosis(diagnosis)
    }
}