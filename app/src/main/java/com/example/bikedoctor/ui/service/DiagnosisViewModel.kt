package com.example.bikedoctor.ui.service

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Diagnosis
import com.example.bikedoctor.data.repository.DiagnosisRepository
import org.json.JSONObject
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

    fun fetchDiagnosis(pageNumber: Int, pageSize: Int, token: String?) {
        Log.d(tag, "Fetching diagnosis: pageNumber=$pageNumber, pageSize=$pageSize")
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
                Log.d(tag, "Role is ADMIN, fetching all diagnosis")
                repository.getDiagnosis(pageNumber, pageSize)
            } else if (role == "EMPLEADO" && ci != null) {
                Log.d(tag, "Role is EMPLEADO, fetching diagnosis for CI=$ci")
                repository.getDiagnosisByEmployee(ci, pageNumber, pageSize)
            } else {
                _isLoading.value = false
                _error.value = "Rol no válido o CI no encontrado"
                Log.e(tag, "Invalid role or CI not found")
                return
            }

            call.enqueue(object : Callback<List<Diagnosis>> {
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