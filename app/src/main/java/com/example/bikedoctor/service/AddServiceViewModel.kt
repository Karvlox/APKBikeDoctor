package com.example.bikedoctor.ui.service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Service
import com.example.bikedoctor.data.repository.ServiceRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddServiceViewModel : ViewModel() {

    private val repository = ServiceRepository()
    private val reasons = mutableListOf<String>()
    private val photos = mutableListOf<String>()

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

    fun validateAndRegister(
        dateTime: String,
        clientId: String,
        motorcycleId: String,
        reason: String
    ) {
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
                val service = Service(
                    dateTime = dateTime,
                    clientId = clientId.toInt(),
                    motorcycleId = motorcycleId,
                    reasons = reasons.toList(),
                    photos = photos.toList()
                )
                registerService(service)
            } catch (e: NumberFormatException) {
                _clientError.value = "El ID del cliente debe ser un número válido"
            }
        }
    }

    fun addReason(reason: String) {
        if (reason.isNotEmpty()) {
            reasons.add(reason)
            _reasonsList.value = reasons.toList()
        }
    }

    fun addPhoto(photoUri: String) {
        if (photos.size < 5) {
            photos.add(photoUri)
            _photosCount.value = photos.size
        } else {
            _registerStatus.value = "Máximo 5 fotos permitidas"
        }
    }

    private fun registerService(service: Service) {
        repository.createService(service).enqueue(object : Callback<Service> {
            override fun onResponse(call: Call<Service>, response: Response<Service>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Servicio registrado exitosamente"
                } else {
                    _registerStatus.value = "Error al registrar: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<Service>, t: Throwable) {
                _registerStatus.value = "Error de conexión: ${t.message}"
            }
        })
    }
}