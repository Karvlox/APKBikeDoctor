package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Repair
import com.example.bikedoctor.data.repository.RepairRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepairViewModel : ViewModel() {

    private val repository = RepairRepository()
    private val tag = "RepairViewModel"

    private val _repairs = MutableLiveData<List<Repair>>()
    val repairs: LiveData<List<Repair>> = _repairs

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
        repository.getRepairs(pageNumber, pageSize).enqueue(object : Callback<List<Repair>> {
            override fun onResponse(call: Call<List<Repair>>, response: Response<List<Repair>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val repairs = response.body() ?: emptyList()
                    val filteredRepairs = repairs.filter { it.reviewed == false }
                    Log.d(tag, "Receptions received: ${filteredRepairs.size}")
                    _repairs.value = filteredRepairs
                    if (filteredRepairs.isEmpty()) {
                        _error.value = "No hay servicios de recepción registrados"
                        Log.d(tag, "No receptions found")
                    }
                } else {
                    val errorMsg = "Error al obtener servicios: ${response.code()} ${response.message()}"
                    _error.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Repair>>, t: Throwable) {
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