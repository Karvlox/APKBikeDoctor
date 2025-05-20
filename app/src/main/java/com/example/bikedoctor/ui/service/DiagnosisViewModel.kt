package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Diagnosis
import com.example.bikedoctor.data.repository.DiagnosisRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DiagnosisViewModel : ViewModel() {

    private val repository = DiagnosisRepository()
    private val tag = "DiagnosisViewModel"

    private val _diagnosis = MutableLiveData<List<Diagnosis>>()
    val diagnosis: LiveData<List<Diagnosis>> = _diagnosis

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchDiagnosis(1, 10)
    }

    fun fetchDiagnosis(pageNumber: Int, pageSize: Int) {
        Log.d(tag, "Fetching diagnosis: pageNumber=$pageNumber, pageSize=$pageSize")
        _isLoading.value = true
        repository.getDiagnosis(pageNumber, pageSize).enqueue(object : Callback<List<Diagnosis>> {
            override fun onResponse(call: Call<List<Diagnosis>>, response: Response<List<Diagnosis>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val diagnosis = response.body() ?: emptyList()
                    val filteredDiagnosis = diagnosis.filter { it.reviewed == false }
                    Log.d(tag, "Diagnosis received: ${filteredDiagnosis.size}")
                    _diagnosis.value = filteredDiagnosis
                    if (filteredDiagnosis.isEmpty()) {
                        _error.value = "No hay diagnósticos pendientes"
                        Log.d(tag, "No pending diagnosis found")
                    }
                } else {
                    val errorMsg = "Error al obtener diagnósticos: ${response.code()} ${response.message()}"
                    _error.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Diagnosis>>, t: Throwable) {
                _isLoading.value = false
                val errorMsg = "Error de conexión: ${t.message}"
                _error.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearError() {
        _error.value = null
    }
}