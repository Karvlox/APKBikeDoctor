package com.example.bikedoctor.ui.service

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.QualityControl
import com.example.bikedoctor.data.repository.ControlRepository
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ControlViewModel : ViewModel() {

    private val repository = ControlRepository()
    private val tag = "ControlViewModel"

    private val _control = MutableLiveData<List<QualityControl>>()
    val control: LiveData<List<QualityControl>> = _control

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchCards(pageNumber: Int, pageSize: Int, token: String?) {
        Log.d(tag, "Fetching controls: pageNumber=$pageNumber, pageSize=$pageSize")
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
                Log.d(tag, "Role is ADMIN, fetching all controls")
                repository.getControls(pageNumber, pageSize)
            } else if (role == "EMPLEADO" && ci != null) {
                Log.d(tag, "Role is EMPLEADO, fetching controls for CI=$ci")
                repository.getControlsByEmployee(ci, pageNumber, pageSize)
            } else {
                _isLoading.value = false
                _error.value = "Rol no válido o CI no encontrado"
                Log.e(tag, "Invalid role or CI not found")
                return
            }

            call.enqueue(object : Callback<List<QualityControl>> {
                override fun onResponse(call: Call<List<QualityControl>>, response: Response<List<QualityControl>>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val controls = response.body() ?: emptyList()
                        val filteredControls = controls.filter { it.reviewed == false }
                        Log.d(tag, "Controls received: ${filteredControls.size}")
                        _control.value = filteredControls
                        if (filteredControls.isEmpty()) {
                            _error.value = "No hay controles de calidad registrados"
                            Log.d(tag, "No controls found")
                        }
                    } else {
                        val errorMsg = "Error al obtener controles: ${response.code()} ${response.message()}"
                        _error.value = errorMsg
                        Log.e(tag, errorMsg)
                    }
                }

                override fun onFailure(call: Call<List<QualityControl>>, t: Throwable) {
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