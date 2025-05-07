package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.SpareParts
import com.example.bikedoctor.data.repository.SparePartsRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SparePartsViewModel : ViewModel() {

    private val repository = SparePartsRepository()
    private val tag = "SparePartsViewModel"

    private val _spareParts = MutableLiveData<List<SpareParts>>()
    val spareParts: LiveData<List<SpareParts>> = _spareParts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchReceptions(1, 10)
    }

    private fun fetchReceptions(pageNumber: Int, pageSize: Int) {
        Log.d(tag, "Fetching receptions: pageNumber=$pageNumber, pageSize=$pageSize")
        _isLoading.value = true
        repository.getSpareParts(pageNumber, pageSize).enqueue(object : Callback<List<SpareParts>> {
            override fun onResponse(call: Call<List<SpareParts>>, response: Response<List<SpareParts>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val diagnosis = response.body() ?: emptyList()
                    Log.d(tag, "Receptions received: ${diagnosis.size}")
                    _spareParts.value = diagnosis
                    if (diagnosis.isEmpty()) {
                        _error.value = "No hay servicios de recepción registrados"
                        Log.d(tag, "No receptions found")
                    }
                } else {
                    val errorMsg = "Error al obtener servicios: ${response.code()} ${response.message()}"
                    _error.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<List<SpareParts>>, t: Throwable) {
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