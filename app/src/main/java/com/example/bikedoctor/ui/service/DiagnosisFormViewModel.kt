package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Diagnostic
import com.example.bikedoctor.data.model.DiagnosisPost
import com.example.bikedoctor.data.repository.DiagnosisRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DiagnosisFormViewModel : ViewModel() {

    private val repository = DiagnosisRepository()
    private val diagnostics = mutableListOf<Diagnostic>()
    private val photos = mutableListOf<String>()
    private val tag = "DiagnosisFormViewModel"
    private var diagnosisId: String? = null

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

    // LiveData para la lista de diagnósticos
    private val _diagnosticsList = MutableLiveData<List<Diagnostic>>()
    val diagnosticsList: LiveData<List<Diagnostic>> = _diagnosticsList

    // LiveData para la cantidad de fotos
    private val _photosCount = MutableLiveData<Int>()
    val photosCount: LiveData<Int> = _photosCount

    // LiveData para las selecciones
    private val _selectedClient = MutableLiveData<Pair<String?, String?>>()
    val selectedClient: LiveData<Pair<String?, String?>> = _selectedClient

    private val _selectedMotorcycle = MutableLiveData<Pair<String?, String?>>()
    val selectedMotorcycle: LiveData<Pair<String?, String?>> = _selectedMotorcycle

    private val _selectedDateTime = MutableLiveData<String?>()
    val selectedDateTime: LiveData<String?> = _selectedDateTime

    private val _reviewed = MutableLiveData<Boolean?>()
    val reviewed: LiveData<Boolean?> = _reviewed

    fun setDateTime(dateTime: String?) {
        _selectedDateTime.value = dateTime
        Log.d(tag, "DateTime set: $dateTime")
    }

    fun setClient(clientCI: String?, clientName: String?) {
        _selectedClient.value = Pair(clientCI, clientName)
        Log.d(tag, "Client set: CI=$clientCI, name=$clientName")
    }

    fun setMotorcycle(motorcycleLicensePlate: String?, motorcycleDetails: String?) {
        _selectedMotorcycle.value = Pair(motorcycleLicensePlate, motorcycleDetails)
        Log.d(tag, "Motorcycle set: licensePlate=$motorcycleLicensePlate, details=$motorcycleDetails")
    }

    fun setReviewed(reviewed: Boolean?) {
        _reviewed.value = reviewed
        Log.d(tag, "Reviewed set: $reviewed")
    }

    fun initializeDiagnosis(
        id: String?,
        date: String?,
        clientCI: String?,
        clientName: String?,
        motorcycleLicensePlate: String?,
        motorcycleDetails: String?,
        employeeCI: String?,
        diagnostics: List<Diagnostic>?,
        images: List<String>?,
        reviewed: Boolean?
    ) {
        diagnosisId = id
        _selectedDateTime.value = date
        _selectedClient.value = Pair(clientCI, clientName ?: "Cliente $clientCI")
        _selectedMotorcycle.value = Pair(motorcycleLicensePlate, motorcycleDetails ?: motorcycleLicensePlate)
        this.diagnostics.clear()
        if (diagnostics != null) {
            this.diagnostics.addAll(diagnostics)
        }
        _diagnosticsList.value = this.diagnostics.toList()
        this.photos.clear()
        if (images != null) {
            this.photos.addAll(images)
        }
        _photosCount.value = this.photos.size
        _reviewed.value = reviewed
        Log.d(tag, "Initialized diagnosis: id=$id, date=$date, clientCI=$clientCI, motorcycleLicensePlate=$motorcycleLicensePlate")
    }

    fun validateAndRegister(
        date: String,
        clientCI: String,
        motorcycleLicensePlate: String,
        error: String,
        errorDetail: String,
        timeSpent: String
    ) {
        Log.d(tag, "Validating: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, error=$error, errorDetail=$errorDetail, timeSpent=$timeSpent")
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

        if (diagnostics.isEmpty() && error.isEmpty()) {
            _errorDiagnosticError.value = "Debe agregar al menos un diagnóstico"
        }

        // Validar diagnóstico si se intenta agregar uno
        if (error.isNotEmpty() || errorDetail.isNotEmpty() || timeSpent.isNotEmpty()) {
            if (error.isEmpty()) {
                _errorDiagnosticError.value = "El nombre del error no puede estar vacío"
            }
            if (errorDetail.isEmpty()) {
                _errorDetailError.value = "La descripción del error no puede estar vacía"
            }
            if (timeSpent.isEmpty()) {
                _timeSpentError.value = "El tiempo invertido no puede estar vacío"
            } else if (!timeSpent.matches(Regex("^\\d+$"))) {
                _timeSpentError.value = "El tiempo debe ser un número entero"
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

                // Agregar diagnóstico si los campos no están vacíos
                if (error.isNotEmpty() && errorDetail.isNotEmpty() && timeSpent.isNotEmpty()) {
                    diagnostics.add(Diagnostic(error, errorDetail, timeSpent.toInt()))
                    _diagnosticsList.value = diagnostics.toList()
                }

                val diagnosis = DiagnosisPost(
                    date = isoDate,
                    clientCI = clientCI.toInt(),
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = 10387210, // Hardcode
                    listDiagnostic = diagnostics.toList(),
                    reviewed = _reviewed.value ?: false
                )

                if (diagnosisId == null) {
                    createDiagnosis(diagnosis)
                } else {
                    updateDiagnosis(diagnosisId!!, diagnosis)
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

    fun addDiagnostic(error: String, errorDetail: String, timeSpent: String) {
        if (error.isNotEmpty() && errorDetail.isNotEmpty() && timeSpent.isNotEmpty()) {
            try {
                val time = timeSpent.toInt()
                diagnostics.add(Diagnostic(error, errorDetail, time))
                _diagnosticsList.value = diagnostics.toList()
                Log.d(tag, "Diagnostic added: error=$error, detail=$errorDetail, time=$time")
            } catch (e: NumberFormatException) {
                _timeSpentError.value = "El tiempo debe ser un número entero"
                Log.e(tag, "Invalid timeSpent format: $timeSpent", e)
            }
        } else {
            if (error.isEmpty()) _errorDiagnosticError.value = "El nombre del error no puede estar vacío"
            if (errorDetail.isEmpty()) _errorDetailError.value = "La descripción no puede estar vacía"
            if (timeSpent.isEmpty()) _timeSpentError.value = "El tiempo invertido no puede estar vacío"
        }
    }

    fun editDiagnostic(index: Int, newError: String, newErrorDetail: String, newTimeSpent: String) {
        if (index in diagnostics.indices && newError.isNotEmpty() && newErrorDetail.isNotEmpty() && newTimeSpent.isNotEmpty()) {
            try {
                val time = newTimeSpent.toInt()
                diagnostics[index] = Diagnostic(newError, newErrorDetail, time)
                _diagnosticsList.value = diagnostics.toList()
                Log.d(tag, "Diagnostic edited at index $index: error=$newError, detail=$newErrorDetail, time=$time")
            } catch (e: NumberFormatException) {
                _timeSpentError.value = "El tiempo debe ser un número entero"
                Log.e(tag, "Invalid timeSpent format: $newTimeSpent", e)
            }
        } else {
            _registerStatus.value = "Todos los campos del diagnóstico deben estar completos"
        }
    }

    fun deleteDiagnostic(index: Int) {
        if (index in diagnostics.indices) {
            val removed = diagnostics.removeAt(index)
            _diagnosticsList.value = diagnostics.toList()
            Log.d(tag, "Diagnostic deleted at index $index: $removed")
        }
    }

    fun addPhoto(photoUri: String) {
        if (photos.size < 5) {
            photos.add(photoUri)
            _photosCount.value = photos.size
            Log.d(tag, "Photo added: $photoUri, total: ${photos.size}")
        } else {
            _registerStatus.value = "Máximo 5 fotos permitidas"
            Log.w(tag, "Max photos reached")
        }
    }

    private fun createDiagnosis(diagnosis: DiagnosisPost) {
        Log.d(tag, "Creating diagnosis: $diagnosis")
        repository.createDiagnosis(diagnosis).enqueue(object : Callback<DiagnosisPost> {
            override fun onResponse(call: Call<DiagnosisPost>, response: Response<DiagnosisPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Diagnóstico registrado exitosamente"
                    Log.d(tag, "Diagnosis created successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al registrar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<DiagnosisPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    private fun updateDiagnosis(id: String, diagnosis: DiagnosisPost) {
        Log.d(tag, "Updating diagnosis with id=$id: $diagnosis")
        repository.updateDiagnosis(id, diagnosis).enqueue(object : Callback<DiagnosisPost> {
            override fun onResponse(call: Call<DiagnosisPost>, response: Response<DiagnosisPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Diagnóstico actualizado exitosamente"
                    Log.d(tag, "Diagnosis updated successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al actualizar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<DiagnosisPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearSelections() {
        diagnosisId = null
        _selectedClient.value = Pair(null, null)
        _selectedMotorcycle.value = Pair(null, null)
        _selectedDateTime.value = null
        _reviewed.value = null
        diagnostics.clear()
        photos.clear()
        _diagnosticsList.value = emptyList()
        _photosCount.value = 0
        Log.d(tag, "Selections cleared")
    }
}