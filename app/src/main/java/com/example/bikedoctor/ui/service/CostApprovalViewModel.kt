package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.repository.CostApprovalRepository
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

    init {
        fetchReceptions(1, 10)
    }

    private fun fetchReceptions(pageNumber: Int, pageSize: Int) {
        Log.d(tag, "Fetching receptions: pageNumber=$pageNumber, pageSize=$pageSize")
        _isLoading.value = true
        repository.getCostApprovals(pageNumber, pageSize).enqueue(object : Callback<List<CostApproval>> {
            override fun onResponse(call: Call<List<CostApproval>>, response: Response<List<CostApproval>>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    val costApproval = response.body() ?: emptyList()
                    val filteredCostAprroval = costApproval.filter { it.reviewed == false }
                    Log.d(tag, "Receptions received: ${filteredCostAprroval.size}")
                    _costApproval.value = filteredCostAprroval
                    if (filteredCostAprroval.isEmpty()) {
                        _error.value = "No hay servicios de recepción registrados"
                        Log.d(tag, "No receptions found")
                    }
                } else {
                    val errorMsg = "Error al obtener servicios: ${response.code()} ${response.message()}"
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
    }

    fun clearError() {
        _error.value = null
    }
}