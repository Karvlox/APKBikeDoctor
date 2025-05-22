package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.SparePart
import com.example.bikedoctor.data.model.SparePartsPost
import com.example.bikedoctor.data.repository.SparePartsRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SparePartsFormViewModel : ViewModel() {

    private val repository = SparePartsRepository()
    private val spareParts = mutableListOf<SparePart>()
    private val tag = "SparePartsFormViewModel"
    private var sparePartId: String? = null

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
    private val _sparePartsList = MutableLiveData<List<SparePart>>()
    val sparePartsList: LiveData<List<SparePart>> = _sparePartsList

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

    fun setSelectedClient(clientCI: String?) {
        _selectedClient.value = clientCI
        Log.d(tag, "ClientCI set: $clientCI")
    }

    fun setSelectedMotorcycle(motorcycleLicensePlate: String?) {
        _selectedMotorcycle.value = motorcycleLicensePlate
        Log.d(tag, "MotorcycleLicensePlate set: $motorcycleLicensePlate")
    }

    fun initializeSpareParts(
        id: String?,
        date: String?,
        clientCI: String?,
        motorcycleLicensePlate: String?,
        employeeCI: String?,
        spareParts: List<SparePart>?,
        reviewed: Boolean?
    ) {
        sparePartId = id
        _selectedDateTime.value = date
        _selectedClient.value = clientCI
        _selectedMotorcycle.value = motorcycleLicensePlate
        this.spareParts.clear()
        if (spareParts != null) {
            this.spareParts.addAll(spareParts)
        }
        _sparePartsList.value = this.spareParts.toList()

        _reviewed.value = reviewed
        Log.d(tag, "Initialized diagnosis: id=$id, date=$date, clientCI=$clientCI, motorcycleLicensePlate=$motorcycleLicensePlate")
    }

    fun validateAndRegister(
        date: String,
        clientCI: String,
        motorcycleLicensePlate: String,
        nameSparePart: String,
        detailSparePart: String,
        price: String
    ) {
        Log.d(tag, "Validating: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, error=$nameSparePart, errorDetail=$detailSparePart, timeSpent=$price")
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

        if (spareParts.isEmpty() && nameSparePart.isEmpty()) {
            _errorDiagnosticError.value = "Debe agregar al menos un repuesto"
        }

        // Validar diagnóstico si se intenta agregar uno
        if (nameSparePart.isNotEmpty() || detailSparePart.isNotEmpty() || price.isNotEmpty()) {
            if (nameSparePart.isEmpty()) {
                _errorDiagnosticError.value = "El nombre del repuesto no puede estar vacío"
            }
            if (detailSparePart.isEmpty()) {
                _errorDetailError.value = "La descripción del repuesto no puede estar vacía"
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
                if (nameSparePart.isNotEmpty() && detailSparePart.isNotEmpty() && price.isNotEmpty()) {
                    spareParts.add(SparePart(nameSparePart, detailSparePart, price.toInt()))
                    _sparePartsList.value = spareParts.toList()
                }

                val sparePart = SparePartsPost(
                    date = isoDate,
                    clientCI = clientCI.toInt(),
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = 10387210, // Hardcode
                    listSpareParts = spareParts.toList(),
                    reviewed = _reviewed.value ?: false
                )

                if (sparePartId == null) {
                    createSparePart(sparePart)
                } else {
                    updateSparePart(sparePartId!!, sparePart)
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

    fun addSparePart(nameSparePart: String, detailSparePart: String, price: String) {
        if (nameSparePart.isNotEmpty() && detailSparePart.isNotEmpty() && price.isNotEmpty()) {
            try {
                val time = price.toInt()
                spareParts.add(SparePart(nameSparePart, detailSparePart, time))
                _sparePartsList.value = spareParts.toList()
                Log.d(tag, "Diagnostic added: error=$nameSparePart, detail=$detailSparePart, time=$time")
            } catch (e: NumberFormatException) {
                _timeSpentError.value = "El tiempo debe ser un número entero"
                Log.e(tag, "Invalid timeSpent format: $price", e)
            }
        } else {
            if (nameSparePart.isEmpty()) _errorDiagnosticError.value = "El nombre del error no puede estar vacío"
            if (detailSparePart.isEmpty()) _errorDetailError.value = "La descripción no puede estar vacía"
            if (price.isEmpty()) _timeSpentError.value = "El tiempo invertido no puede estar vacío"
        }
    }

    fun editSparePart(index: Int, newSparePart: String, newdetailSparePart: String, newPrice: String) {
        if (index in spareParts.indices && newSparePart.isNotEmpty() && newdetailSparePart.isNotEmpty() && newPrice.isNotEmpty()) {
            try {
                val time = newPrice.toInt()
                spareParts[index] = SparePart(newSparePart, newdetailSparePart, time)
                _sparePartsList.value = spareParts.toList()
                Log.d(tag, "Diagnostic edited at index $index: error=$newSparePart, detail=$newdetailSparePart, time=$time")
            } catch (e: NumberFormatException) {
                _timeSpentError.value = "El precio debe ser un número entero"
                Log.e(tag, "Invalid timeSpent format: $newPrice", e)
            }
        } else {
            _registerStatus.value = "Todos los campos del diagnóstico deben estar completos"
        }
    }

    fun deleteSparePart(index: Int) {
        if (index in spareParts.indices) {
            val removed = spareParts.removeAt(index)
            _sparePartsList.value = spareParts.toList()
            Log.d(tag, "Spare Part deleted at index $index: $removed")
        }
    }

    private fun createSparePart(spareParts: SparePartsPost) {
        Log.d(tag, "Creating Spare Parts: $spareParts")
        repository.createSpareParts(spareParts).enqueue(object : Callback<SparePartsPost> {
            override fun onResponse(call: Call<SparePartsPost>, response: Response<SparePartsPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Repuesto registrado exitosamente"
                    Log.d(tag, "Spare Part created successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al registrar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<SparePartsPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    private fun updateSparePart(id: String, sparePart: SparePartsPost) {
        Log.d(tag, "Updating spare part with id=$id: $sparePart")
        repository.updateSpareParts(id, sparePart).enqueue(object : Callback<SparePartsPost> {
            override fun onResponse(call: Call<SparePartsPost>, response: Response<SparePartsPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Repuesto actualizado exitosamente"
                    Log.d(tag, "Spare Part updated successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al actualizar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<SparePartsPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearSelections() {
        sparePartId = null
        _selectedClient.value = null
        _selectedMotorcycle.value = null
        _selectedDateTime.value = null
        _reviewed.value = null
        spareParts.clear()
        _sparePartsList.value = emptyList()
        Log.d(tag, "Selections cleared")
    }
}