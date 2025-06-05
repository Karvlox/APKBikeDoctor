package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.repository.ReceptionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReceptionViewModel : ViewModel() {

    private val repository = ReceptionRepository()
    private val tag = "ReceptionViewModel"

    private val _receptions = MutableLiveData<List<Reception>>()
    val receptions: LiveData<List<Reception>> = _receptions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchReceptions(1, 100)
    }

    fun fetchReceptions(pageNumber: Int, pageSize: Int) {
        Log.d(tag, "Fetching receptions: pageNumber=$pageNumber, pageSize=$pageSize")
        _isLoading.value = true
        repository.getReceptions(pageNumber, pageSize).enqueue(object : Callback<List<Reception>> {
            override fun onResponse(call: Call<List<Reception>>, response: Response<List<Reception>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val receptions = response.body() ?: emptyList()
                    val filteredReceptions = receptions.filter { it.reviewed == false }
                    Log.d(tag, "Filtered receptions: ${filteredReceptions.size}")
                    _receptions.value = filteredReceptions
                    if (filteredReceptions.isEmpty()) {
                        _error.value = "No hay servicios de recepción pendientes"
                        Log.d(tag, "No pending receptions found")
                    }
                } else {
                    val errorMsg = "Error al obtener servicios: ${response.code()} ${response.message()}"
                    _error.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Reception>>, t: Throwable) {
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