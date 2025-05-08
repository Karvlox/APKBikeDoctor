package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.repository.ReceptionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddServiceViewModel : ViewModel() {

    private val repository = ReceptionRepository()
    private val reasons = mutableListOf<String>()
    private val photos = mutableListOf<String>()
    private val tag = "AddServiceViewModel"

    // LiveData para los errores de validación
    private val _dateTimeError = MutableLiveData<String?>()
    val dateTimeError: LiveData<String?> = _dateTimeError

    private val _clientError = MutableLiveData<String?>()
    val clientError: LiveData<String?> = _clientError

    private val _motorcycleError = MutableLiveData<String?>()
    val motorcycleError: LiveData<String?> = _motorcycleError

    private val _reasonError = MutableLiveData<String?>()
    val reasonError: LiveData<String?> = _reasonError

    // LiveData para el estado del registro
    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> = _registerStatus

    // LiveData para la lista de motivos
    private val _reasonsList = MutableLiveData<List<String>>()
    val reasonsList: LiveData<List<String>> = _reasonsList

    // LiveData para la cantidad de fotos
    private val _photosCount = MutableLiveData<Int>()
    val photosCount: LiveData<Int> = _photosCount

    // LiveData para las selecciones de cliente y motocicleta
    private val _selectedClient = MutableLiveData<Pair<String?, String?>>()
    val selectedClient: LiveData<Pair<String?, String?>> = _selectedClient

    private val _selectedMotorcycle = MutableLiveData<Pair<String?, String?>>()
    val selectedMotorcycle: LiveData<Pair<String?, String?>> = _selectedMotorcycle

    fun setClient(clientId: String?, clientName: String?) {
        _selectedClient.value = Pair(clientId, clientName)
        Log.d(tag, "Client selected: id=$clientId, name=$clientName")
    }

    fun setMotorcycle(motorcycleId: String?, motorcycleDetails: String?) {
        _selectedMotorcycle.value = Pair(motorcycleId, motorcycleDetails)
        Log.d(tag, "Motorcycle selected: id=$motorcycleId, details=$motorcycleDetails")
    }

    fun validateAndRegister(
        dateTime: String,
        clientId: String,
        motorcycleId: String,
        reason: String
    ) {
        Log.d(tag, "Validating: dateTime=$dateTime, clientId=$clientId, motorcycleId=$motorcycleId, reason=$reason")
        // Limpiar errores previos
        _dateTimeError.value = null
        _clientError.value = null
        _motorcycleError.value = null
        _reasonError.value = null

        // Validar campos
        if (dateTime.isEmpty()) {
            _dateTimeError.value = "La fecha y hora no pueden estar vacías"
        } else if (!dateTime.matches(Regex("^\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2} (AM|PM)$"))) {
            _dateTimeError.value = "Formato inválido (ej. 28-03-2025 08:00 AM)"
        }

        if (clientId.isEmpty()) {
            _clientError.value = "Debe seleccionar un cliente"
        }

        if (motorcycleId.isEmpty()) {
            _motorcycleError.value = "Debe seleccionar una motocicleta"
        }

        if (reason.isEmpty()) {
            _reasonError.value = "El motivo no puede estar vacío"
        }

        // Verificar si todos los campos son válidos
        if (_dateTimeError.value == null &&
            _clientError.value == null &&
            _motorcycleError.value == null &&
            _reasonError.value == null
        ) {
            try {
                val reception = Reception(
                    id = "",
                    date = dateTime,
                    clientCI = clientId.toInt(),
                    motorcycleLicensePlate = motorcycleId,
                    employeeCI = 2,
                    reasons = reasons.toList(),
                    images = photos.toList()
                )
                registerReception(reception)
            } catch (e: NumberFormatException) {
                _clientError.value = "El ID del cliente debe ser un número válido"
                Log.e(tag, "Invalid clientId format: $clientId", e)
            }
        }
    }

    fun addReason(reason: String) {
        if (reason.isNotEmpty()) {
            reasons.add(reason)
            _reasonsList.value = reasons.toList()
            Log.d(tag, "Reason added: $reason, total: ${reasons.size}")
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

    private fun registerReception(reception: Reception) {
        Log.d(tag, "Registering reception: $reception")
        repository.createReception(reception).enqueue(object : Callback<Reception> {
            override fun onResponse(call: Call<Reception>, response: Response<Reception>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Servicio registrado exitosamente"
                    Log.d(tag, "Reception registered successfully")
                    // Limpiar listas después de un registro exitoso
                    reasons.clear()
                    photos.clear()
                    _reasonsList.value = emptyList()
                    _photosCount.value = 0
                } else {
                    val errorMsg = "Error al registrar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<Reception>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearSelections() {
        _selectedClient.value = Pair(null, null)
        _selectedMotorcycle.value = Pair(null, null)
        reasons.clear()
        photos.clear()
        _reasonsList.value = emptyList()
        _photosCount.value = 0
        Log.d(tag, "Selections cleared")
    }
}