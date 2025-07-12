package com.example.bikedoctor.ui.service

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Repair
import com.example.bikedoctor.data.repository.RepairRepository
import org.json.JSONObject
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

    fun fetchRepairs(pageNumber: Int, pageSize: Int, token: String?) {
        Log.d(tag, "Fetching repairs: pageNumber=$pageNumber, pageSize=$pageSize")
        _isLoading.value = true

        if (token == null) {
            _isLoading.value = false
            _error.value = "No se encontró el token de autenticación"
            Log.e(tag, "No token found")
            return
        }

        try {
            // Decodificar el token para obtener Role y Ci
            val payload = token.split(".")[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedPayload = String(decodedBytes, Charsets.UTF_8)
            val jsonPayload = JSONObject(decodedPayload)
            val role = jsonPayload.getString("Role")
            val ci = jsonPayload.getString("Ci").toIntOrNull()

            // Decidir qué request realizar según el rol
            val call = if (role == "ADMIN") {
                Log.d(tag, "Role is ADMIN, fetching all repairs")
                repository.getRepairs(pageNumber, pageSize)
            } else if (role == "EMPLEADO" && ci != null) {
                Log.d(tag, "Role is EMPLEADO, fetching repairs for CI=$ci")
                repository.getRepairsByEmployee(ci, pageNumber, pageSize)
            } else {
                _isLoading.value = false
                _error.value = "Rol no válido o CI no encontrado"
                Log.e(tag, "Invalid role or CI not found")
                return
            }

            // Ejecutar el request
            call.enqueue(object : Callback<List<Repair>> {
                override fun onResponse(call: Call<List<Repair>>, response: Response<List<Repair>>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val repairs = response.body() ?: emptyList()
                        val filteredRepairs = repairs.filter { it.reviewed == false }
                        Log.d(tag, "Repairs received: ${filteredRepairs.size}")
                        _repairs.value = filteredRepairs
                        if (filteredRepairs.isEmpty()) {
                            _error.value = "No hay reparaciones pendientes"
                            Log.d(tag, "No pending repairs found")
                        }
                    } else {
                        val errorMsg = "Error al obtener reparaciones: ${response.code()} ${response.message()}"
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