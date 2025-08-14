package com.example.bikedoctor.ui.service

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.repository.ReceptionRepository
import org.json.JSONObject
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

    fun fetchReceptions(pageNumber: Int, pageSize: Int, token: String?) {
        Log.d(tag, "Fetching receptions: pageNumber=$pageNumber, pageSize=$pageSize")
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
                Log.d(tag, "Role is ADMIN, fetching all receptions")
                repository.getReceptions(pageNumber, pageSize)
            } else if (role == "EMPLEADO" && ci != null) {
                Log.d(tag, "Role is EMPLEADO, fetching receptions for CI=$ci")
                repository.getReceptionsByEmployee(ci, pageNumber, pageSize)
            } else {
                _isLoading.value = false
                _error.value = "Rol no válido o CI no encontrado"
                Log.e(tag, "Invalid role or CI not found")
                return
            }

            call.enqueue(object : Callback<List<Reception>> {
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