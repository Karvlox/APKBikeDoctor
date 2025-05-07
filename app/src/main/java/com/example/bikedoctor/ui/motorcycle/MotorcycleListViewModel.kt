package com.example.bikedoctor.ui.motorcycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Motorcycle
import com.example.bikedoctor.data.repository.MotorcycleRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MotorcycleListViewModel : ViewModel() {

    private val repository = MotorcycleRepository()

    private val _motorcycles = MutableLiveData<List<Motorcycle>>()
    val motorcycles: LiveData<List<Motorcycle>> = _motorcycles

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchMotorcycles()
    }

    private fun fetchMotorcycles() {
        _isLoading.value = true
        repository.getMotorcycles().enqueue(object : Callback<List<Motorcycle>> {
            override fun onResponse(call: Call<List<Motorcycle>>, response: Response<List<Motorcycle>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _motorcycles.value = response.body() ?: emptyList()
                    if (_motorcycles.value!!.isEmpty()) {
                        _error.value = "No hay motocicletas registradas"
                    }
                } else {
                    _error.value = "Error al obtener motocicletas: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<Motorcycle>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    fun clearError() {
        _error.value = null
    }
}