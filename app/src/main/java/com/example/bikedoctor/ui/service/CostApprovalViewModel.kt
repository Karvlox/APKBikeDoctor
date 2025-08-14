package com.example.bikedoctor.ui.service

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.repository.CostApprovalRepository
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CostApprovalViewModel : ViewModel() {

    private val repository = CostApprovalRepository()
    private val tag = "CostApprovalViewModel"

    private val _costApproval = MutableLiveData<List<CostApproval>>()
    val costApproval: LiveData<List<CostApproval>> = _costApproval

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchCostApprovals(pageNumber: Int, pageSize: Int, token: String?) {
        Log.d(tag, "Fetching cost approvals: pageNumber=$pageNumber, pageSize=$pageSize")
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
                Log.d(tag, "Role is ADMIN, fetching all cost approvals")
                repository.getCostApprovals(pageNumber, pageSize)
            } else if (role == "EMPLEADO" && ci != null) {
                Log.d(tag, "Role is EMPLEADO, fetching cost approvals for CI=$ci")
                repository.getCostApprovalsByEmployee(ci, pageNumber, pageSize)
            } else {
                _isLoading.value = false
                _error.value = "Rol no válido o CI no encontrado"
                Log.e(tag, "Invalid role or CI not found")
                return
            }

            call.enqueue(object : Callback<List<CostApproval>> {
                override fun onResponse(call: Call<List<CostApproval>>, response: Response<List<CostApproval>>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val costApprovals = response.body() ?: emptyList()
                        val filteredCostApprovals = costApprovals.filter { it.reviewed == false }
                        Log.d(tag, "Cost approvals received: ${filteredCostApprovals.size}")
                        _costApproval.value = filteredCostApprovals
                        if (filteredCostApprovals.isEmpty()) {
                            _error.value = "No hay aprobaciones de costos pendientes"
                            Log.d(tag, "No pending cost approvals found")
                        }
                    } else {
                        val errorMsg = "Error al obtener aprobaciones de costos: ${response.code()} ${response.message()}"
                        _error.value = errorMsg
                        Log.e(tag, errorMsg)
                    }
                }

                override fun onFailure(call: Call<List<CostApproval>>, t: Throwable) {
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