package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.LaborCost
import com.example.bikedoctor.data.repository.CostApprovalRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class CostApprovalFormViewModel : ViewModel() {

    private val repository = CostApprovalRepository()
    private val costApprovals = mutableListOf<LaborCost>()
    private val tag = "SparePartsFormViewModel"
    private var costApprovalId: String? = null

    // LiveData para errores de validación
    private val _dateTimeError = MutableLiveData<String?>()
    val dateTimeError: LiveData<String?> = _dateTimeError

    private val _clientError = MutableLiveData<String?>()
    val clientError: LiveData<String?> = _clientError

    private val _motorcycleError = MutableLiveData<String?>()
    val motorcycleError: LiveData<String?> = _motorcycleError

    private val _errorDiagnosticError = MutableLiveData<String?>()
    val errorDiagnosticError: LiveData<String?> = _errorDiagnosticError

    private val _errorDetailError = MutableLiveData<String?>()
    val errorDetailError: LiveData<String?> = _errorDetailError

    private val _timeSpentError = MutableLiveData<String?>()
    val timeSpentError: LiveData<String?> = _timeSpentError

    // LiveData para el estado del registro
    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> = _registerStatus

    // LiveData para la lista de Repuestos
    private val _costApprovalList = MutableLiveData<List<LaborCost>>()
    val costApprovalList: LiveData<List<LaborCost>> = _costApprovalList

    private val _selectedDateTime = MutableLiveData<String?>()
    val selectedDateTime: LiveData<String?> = _selectedDateTime

    // LiveData para las selecciones
    private val _selectedClient = MutableLiveData<String?>()
    val selectedClient: LiveData<String?> = _selectedClient

    private val _selectedMotorcycle = MutableLiveData<String?>()
    val selectedMotorcycle: LiveData<String?> = _selectedMotorcycle

    private val _reviewed = MutableLiveData<Boolean?>()
    val reviewed: LiveData<Boolean?> = _reviewed

    fun setDateTime(dateTime: String?) {
        _selectedDateTime.value = dateTime
        Log.d(tag, "DateTime set: $dateTime")
    }

    fun initializeCostApproval(
        id: String?,
        date: String?,
        clientCI: String?,
        motorcycleLicensePlate: String?,
        employeeCI: String?,
        costApprovals: List<LaborCost>?,
        reviewed: Boolean?
    ) {
        costApprovalId = id
        _selectedDateTime.value = date
        _selectedClient.value = clientCI
        _selectedMotorcycle.value = motorcycleLicensePlate
        this.costApprovals.clear()
        if (costApprovals != null) {
            this.costApprovals.addAll(costApprovals)
        }
        _costApprovalList.value = this.costApprovals.toList()

        _reviewed.value = reviewed
        Log.d(tag, "Initialized diagnosis: id=$id, date=$date, clientCI=$clientCI, motorcycleLicensePlate=$motorcycleLicensePlate")
    }

    fun validateAndRegister(
        date: String,
        clientCI: String,
        motorcycleLicensePlate: String,
        nameCostApproval: String,
        detailCostApproval: String,
        price: String
    ) {
        Log.d(tag, "Validating: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, error=$nameCostApproval, errorDetail=$detailCostApproval, timeSpent=$price")
        // Limpiar errores previos
        _dateTimeError.value = null
        _clientError.value = null
        _motorcycleError.value = null
        _errorDiagnosticError.value = null
        _errorDetailError.value = null
        _timeSpentError.value = null

        // Validar campos
        if (date.isEmpty()) {
            _dateTimeError.value = "La fecha y hora no pueden estar vacías"
        } else if (!date.matches(Regex("^\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2} (AM|PM)$"))) {
            _dateTimeError.value = "Formato inválido (ej. 28-03-2025 08:00 AM)"
        }

        if (clientCI.isEmpty()) {
            _clientError.value = "El cliente no puede estar vacío"
        } else if (!clientCI.matches(Regex("^\\d+$"))) {
            _clientError.value = "El CI del cliente debe contener solo números"
        }

        if (motorcycleLicensePlate.isEmpty()) {
            _motorcycleError.value = "La motocicleta no puede estar vacía"
        }

        if (costApprovals.isEmpty() && nameCostApproval.isEmpty()) {
            _errorDiagnosticError.value = "Debe agregar al menos una aprobacion de costo"
        }

        // Validar diagnóstico si se intenta agregar uno
        if (nameCostApproval.isNotEmpty() || detailCostApproval.isNotEmpty() || price.isNotEmpty()) {
            if (nameCostApproval.isEmpty()) {
                _errorDiagnosticError.value = "El nombre no puede estar vacío"
            }
            if (detailCostApproval.isEmpty()) {
                _errorDetailError.value = "La descripción no puede estar vacía"
            }
            if (price.isEmpty()) {
                _timeSpentError.value = "El precio no puede estar vacío"
            } else if (!price.matches(Regex("^\\d+$"))) {
                _timeSpentError.value = "El precio debe ser un número entero"
            }
        }

        // Verificar si todos los campos son válidos
        if (_dateTimeError.value == null &&
            _clientError.value == null &&
            _motorcycleError.value == null &&
            _errorDiagnosticError.value == null &&
            _errorDetailError.value == null &&
            _timeSpentError.value == null
        ) {
            try {
                // Convertir fecha a formato ISO 8601
                val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val parsedDate = inputFormat.parse(date)
                val isoDate = outputFormat.format(parsedDate)

                // Agregar repuesto si los campos no están vacíos
                if (nameCostApproval.isNotEmpty() && detailCostApproval.isNotEmpty() && price.isNotEmpty()) {
                    costApprovals.add(LaborCost(nameCostApproval, detailCostApproval, price))
                    _costApprovalList.value = costApprovals.toList()
                }

                val costApproval = CostApprovalPost(
                    date = isoDate,
                    clientCI = clientCI.toInt(),
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = 10387210, // Hardcode
                    listLaborCosts = costApprovals.toList(),
                    reviewed = _reviewed.value ?: false
                )

                if (costApprovalId == null) {
                    createCostApproval(costApproval)
                } else {
                    updateCostApproval(costApprovalId!!, costApproval)
                }
            } catch (e: NumberFormatException) {
                _clientError.value = "El CI del cliente debe ser un número válido"
                Log.e(tag, "Invalid clientCI format: $clientCI", e)
            } catch (e: Exception) {
                _dateTimeError.value = "Error al procesar la fecha"
                Log.e(tag, "Date parsing error: $date", e)
            }
        }
    }

    fun addSparePart(nameCostApproval: String, detailCostApproval: String, price: String) {
        if (nameCostApproval.isNotEmpty() && detailCostApproval.isNotEmpty() && price.isNotEmpty()) {
            try {
                costApprovals.add(LaborCost(nameCostApproval, detailCostApproval, price))
                _costApprovalList.value = costApprovals.toList()
                Log.d(tag, "CostApproval added: title=$nameCostApproval, detail=$detailCostApproval, time=$price")
            } catch (e: NumberFormatException) {
                _timeSpentError.value = "El tiempo debe ser un número entero"
                Log.e(tag, "Invalid timeSpent format: $price", e)
            }
        } else {
            if (nameCostApproval.isEmpty()) _errorDiagnosticError.value = "El nombre no puede estar vacío"
            if (detailCostApproval.isEmpty()) _errorDetailError.value = "La descripción no puede estar vacía"
            if (price.isEmpty()) _timeSpentError.value = "El precio no puede estar vacío"
        }
    }

    fun editSparePart(index: Int, newCostApproval: String, newdetailCostApproval: String, newPrice: String) {
        if (index in costApprovals.indices && newCostApproval.isNotEmpty() && newdetailCostApproval.isNotEmpty() && newPrice.isNotEmpty()) {
            try {
                costApprovals[index] = LaborCost(newCostApproval, newdetailCostApproval, newPrice)
                _costApprovalList.value = costApprovals.toList()
                Log.d(
                    tag,
                    "Cost Approval edited at index $index: title=$newCostApproval, detail=$newdetailCostApproval, time=$newPrice"
                )
            } catch (e: NumberFormatException) {
                _timeSpentError.value = "El precio debe ser un número entero"
                Log.e(tag, "Invalid timeSpent format: $newPrice", e)
            }
        } else {
            _registerStatus.value = "Todos los campos deben estar completos"
        }
    }

    fun deleteCostApproval(index: Int) {
        if (index in costApprovals.indices) {
            val removed = costApprovals.removeAt(index)
            _costApprovalList.value = costApprovals.toList()
            Log.d(tag, "Spare Part deleted at index $index: $removed")
        }
    }


    private fun createCostApproval(costApproval: CostApprovalPost) {
        Log.d(tag, "Creating Cost Approval: $costApproval")
        repository.createCostApprovals(costApproval).enqueue(object : Callback<CostApprovalPost> {
        override fun onResponse(call: Call<CostApprovalPost>, response: Response<CostApprovalPost>) {
            if (response.isSuccessful) {
                _registerStatus.value = "Aprobacion de costo registrado exitosamente"
                Log.d(tag, "Spare Part created successfully")
                clearSelections()
            } else {
                val errorMsg = "Error al registrar: ${response.code()} ${response.message()}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg)
            }
        }

        override fun onFailure(call: Call<CostApprovalPost>, t: Throwable) {
            val errorMsg = "Error de conexión: ${t.message}"
            _registerStatus.value = errorMsg
            Log.e(tag, errorMsg, t)
            }
        })
    }

    private fun updateCostApproval(id: String, costAppoval: CostApprovalPost) {
        Log.d(tag, "Updating cost approval with id=$id: $costAppoval")
        repository.updateCostApprovals(id, costAppoval).enqueue(object : Callback<CostApprovalPost> {
        override fun onResponse(call: Call<CostApprovalPost>, response: Response<CostApprovalPost>) {
        if (response.isSuccessful) {
            _registerStatus.value = "Repuesto actualizado exitosamente"
            Log.d(tag, "Cost Approval updated successfully")
            clearSelections()
        } else {
            val errorMsg = "Error al actualizar: ${response.code()} ${response.message()}"
            _registerStatus.value = errorMsg
            Log.e(tag, errorMsg)
            }
        }

        override fun onFailure(call: Call<CostApprovalPost>, t: Throwable) {
            val errorMsg = "Error de conexión: ${t.message}"
            _registerStatus.value = errorMsg
            Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearSelections() {
        costApprovalId = null
        _selectedClient.value = null
        _selectedMotorcycle.value = null
        _selectedDateTime.value = null
        _reviewed.value = null
        costApprovals.clear()
        _costApprovalList.value = emptyList()
        Log.d(tag, "Selections cleared")
    }

}

