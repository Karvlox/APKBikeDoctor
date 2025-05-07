package com.example.bikedoctor.ui.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.repository.ClientRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientsListViewModel : ViewModel() {

    private val repository = ClientRepository()

    // LiveData para la lista de clientes
    private val _clients = MutableLiveData<List<Client>>()
    val clients: LiveData<List<Client>> = _clients

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        fetchClients()
    }

    private fun fetchClients() {
        _isLoading.value = true
        repository.getClients().enqueue(object : Callback<List<Client>> {
            override fun onResponse(call: Call<List<Client>>, response: Response<List<Client>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _clients.value = response.body() ?: emptyList()
                    if (_clients.value!!.isEmpty()) {
                        _error.value = "No hay clientes registrados"
                    }
                } else {
                    _error.value = "Error al obtener clientes: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<List<Client>>, t: Throwable) {
                _isLoading.value = false
                _error.value = "Error de conexión: ${t.message}"
            }
        })
    }

    fun clearError() {
        _error.value = null
    }
}