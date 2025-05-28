package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.RepairPost
import com.example.bikedoctor.data.model.Reparation
import com.example.bikedoctor.data.repository.RepairRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class RepairFormViewModel : ViewModel() {

    private val repository = RepairRepository()
    private val reparations = mutableListOf<Reparation>()
    private val tag = "RepairFormViewModel"
    private var repairId: String? = null

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

    // LiveData para el estado del registro
    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> = _registerStatus

    // LiveData para la lista de Repuestos
    private val _repairsList = MutableLiveData<List<Reparation>>()
    val repairsList: LiveData<List<Reparation>> = _repairsList

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

    fun initializeRepair(
        id: String?,
        date: String?,
        clientCI: String?,
        motorcycleLicensePlate: String?,
        employeeCI: String?,
        reparations: List<Reparation>?,
        reviewed: Boolean?
    ) {
        repairId = id
        _selectedDateTime.value = date
        _selectedClient.value = clientCI
        _selectedMotorcycle.value = motorcycleLicensePlate
        this.reparations.clear()
        if (reparations != null) {
            this.reparations.addAll(reparations)
        }
        _repairsList.value = this.reparations.toList()

        _reviewed.value = reviewed
        Log.d(tag, "Initialized diagnosis: id=$id, date=$date, clientCI=$clientCI, motorcycleLicensePlate=$motorcycleLicensePlate")
    }

    fun validateAndRegister(
        date: String,
        clientCI: String,
        motorcycleLicensePlate: String,
        nameReparation: String,
        detailReparation: String,
    ) {
        Log.d(tag, "Validating: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, error=$nameReparation, errorDetail=$detailReparation")
        // Limpiar errores previos
        _dateTimeError.value = null
        _clientError.value = null
        _motorcycleError.value = null
        _errorDiagnosticError.value = null
        _errorDetailError.value = null

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

        if (reparations.isEmpty() && nameReparation.isEmpty()) {
            _errorDiagnosticError.value = "Debe agregar al menos una reparación"
        }

        // Validar Reparacion si se intenta agregar uno
        if (nameReparation.isNotEmpty() || detailReparation.isNotEmpty()) {
            if (nameReparation.isEmpty()) {
                _errorDiagnosticError.value = "El nombre no puede estar vacío"
            }
            if (detailReparation.isEmpty()) {
                _errorDetailError.value = "La descripción no puede estar vacía"
            }
        }

        // Verificar si todos los campos son válidos
        if (_dateTimeError.value == null &&
            _clientError.value == null &&
            _motorcycleError.value == null &&
            _errorDiagnosticError.value == null &&
            _errorDetailError.value == null
        ) {
            try {
                // Convertir fecha a formato ISO 8601
                val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val parsedDate = inputFormat.parse(date)
                val isoDate = outputFormat.format(parsedDate)

                // Agregar repuesto si los campos no están vacíos
                if (nameReparation.isNotEmpty() && detailReparation.isNotEmpty()) {
                    reparations.add(Reparation(nameReparation, detailReparation))
                    _repairsList.value = reparations.toList()
                }

                val repair = RepairPost(
                    date = isoDate,
                    clientCI = clientCI.toInt(),
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = 10387210, // Hardcode
                    listReparations = reparations.toList(),
                    reviewed = _reviewed.value ?: false
                )

                if (repairId == null) {
                    createRepair(repair)
                } else {
                    updateRepair(repairId!!, repair)
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

    fun addSparePart(name: String, detail: String) {
        if (name.isNotEmpty() && detail.isNotEmpty()) {
            try {
                reparations.add(Reparation(name, detail))
                _repairsList.value = reparations.toList()
                Log.d(tag, "Repair added: title=$name, detail=$detail")
            } catch (e: NumberFormatException) {

            }
        } else {
            if (name.isEmpty()) _errorDiagnosticError.value = "El nombre no puede estar vacío"
            if (detail.isEmpty()) _errorDetailError.value = "La descripción no puede estar vacía"
        }
    }

    fun editSparePart(index: Int, newReparation: String, newDetailReparation: String) {
        if (index in reparations.indices && newReparation.isNotEmpty() && newDetailReparation.isNotEmpty()) {
            try {
                reparations[index] = Reparation(newReparation, newDetailReparation)
                _repairsList.value = reparations.toList()
                Log.d(
                    tag,
                    "Reparation edited at index $index: title=$newReparation, detail=$newDetailReparation"
                )
            } catch (e: NumberFormatException) {
            }
        } else {
            _registerStatus.value = "Todos los campos deben estar completos"
        }
    }

    fun deleteRepair(index: Int) {
        if (index in reparations.indices) {
            val removed = reparations.removeAt(index)
            _repairsList.value = reparations.toList()
            Log.d(tag, "Spare Part deleted at index $index: $removed")
        }
    }

    private fun createRepair(repair: RepairPost) {
        Log.d(tag, "Creating Repair: $repair")
        repository.createRepairs(repair).enqueue(object : Callback<RepairPost> {
            override fun onResponse(call: Call<RepairPost>, response: Response<RepairPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Reparación registrado exitosamente"
                    Log.d(tag, "Spare Part created successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al registrar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<RepairPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    private fun updateRepair(id: String, repair: RepairPost) {
        Log.d(tag, "Updating repair with id=$id: $repair")
        repository.updateRepairs(id, repair).enqueue(object : Callback<RepairPost> {
            override fun onResponse(call: Call<RepairPost>, response: Response<RepairPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Repuesto actualizado exitosamente"
                    Log.d(tag, "Repair updated successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al actualizar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<RepairPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearSelections() {
        repairId = null
        _selectedClient.value = null
        _selectedMotorcycle.value = null
        _selectedDateTime.value = null
        _reviewed.value = null
        reparations.clear()
        _repairsList.value = emptyList()
        Log.d(tag, "Selections cleared")
    }
}