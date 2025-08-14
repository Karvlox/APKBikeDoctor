package com.example.bikedoctor.ui.service

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.repository.DeliveryRepository
import org.json.JSONObject
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

    fun fetchDeliveries(pageNumber: Int, pageSize: Int, token: String?) {
        Log.d(tag, "Fetching deliveries: pageNumber=$pageNumber, pageSize=$pageSize")
        _isLoading.value = true

        if (token == null) {
            _isLoading.value = false
            _error.value = "No se encontró el token de autenticación"
            Log.e(tag, "No token found")
            return
        }

        try {
            val payload = token.split(".")[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedPayload = String(decodedBytes, Charsets.UTF_8)
            val jsonPayload = JSONObject(decodedPayload)
            val role = jsonPayload.getString("Role")
            val ci = jsonPayload.getString("Ci").toIntOrNull()

            val call = if (role == "ADMIN") {
                Log.d(tag, "Role is ADMIN, fetching all deliveries")
                repository.getDelivery(pageNumber, pageSize)
            } else if (role == "EMPLEADO" && ci != null) {
                Log.d(tag, "Role is EMPLEADO, fetching deliveries for CI=$ci")
                repository.getDeliveriesByEmployee(ci, pageNumber, pageSize)
            } else {
                _isLoading.value = false
                _error.value = "Rol no válido o CI no encontrado"
                Log.e(tag, "Invalid role or CI not found")
                return
            }

            call.enqueue(object : Callback<List<Delivery>> {
                override fun onResponse(call: Call<List<Delivery>>, response: Response<List<Delivery>>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val deliveries = response.body() ?: emptyList()
                        val filteredDeliveries = deliveries.filter { it.reviewed == false }
                        Log.d(tag, "Deliveries received: ${filteredDeliveries.size}")
                        _delivery.value = filteredDeliveries
                        if (filteredDeliveries.isEmpty()) {
                            _error.value = "No hay entregas pendientes"
                            Log.d(tag, "No pending deliveries found")
                        }
                    } else {
                        val errorMsg = "Error al obtener entregas: ${response.code()} ${response.message()}"
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

        } catch (e: Exception) {
            _isLoading.value = false
            val errorMsg = "Error al decodificar el token: ${e.message}"
            _error.value = errorMsg
            Log.e(tag, errorMsg, e)
        }
    }

    fun clearError() {
        _error.value = null
    }
}