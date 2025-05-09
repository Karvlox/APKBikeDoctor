package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.repository.DeliveryRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeliveryViewModel : ViewModel() {

    private val repository = DeliveryRepository()
    private val tag = "DeliveryViewModel"

    private val _delivery = MutableLiveData<List<Delivery>>()
    val delivery: LiveData<List<Delivery>> = _delivery

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
        repository.getDelivery(pageNumber, pageSize).enqueue(object : Callback<List<Delivery>> {
            override fun onResponse(call: Call<List<Delivery>>, response: Response<List<Delivery>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val delivery = response.body() ?: emptyList()
                    val filteredDelivery = delivery.filter { it.reviewed == false }
                    Log.d(tag, "Receptions received: ${filteredDelivery.size}")
                    _delivery.value = filteredDelivery
                    if (filteredDelivery.isEmpty()) {
                        _error.value = "No hay servicios de recepción registrados"
                        Log.d(tag, "No receptions found")
                    }
                } else {
                    val errorMsg = "Error al obtener servicios: ${response.code()} ${response.message()}"
                    _error.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Delivery>>, t: Throwable) {
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