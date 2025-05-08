package com.example.bikedoctor.ui.service

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.ReceptionSend
import com.example.bikedoctor.data.repository.ReceptionRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

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

    // LiveData para la fecha seleccionada
    private val _selectedDateTime = MutableLiveData<String?>()
    val selectedDateTime: LiveData<String?> = _selectedDateTime

    fun setDateTime(dateTime: String?) {
        _selectedDateTime.value = dateTime
        Log.d(tag, "DateTime set: $dateTime")
    }

    fun setClient(clientCI: String?, clientName: String?) {
        _selectedClient.value = Pair(clientCI, clientName)
        Log.d(tag, "Client selected: CI=$clientCI, name=$clientName")
    }

    fun setMotorcycle(motorcycleLicensePlate: String?, motorcycleDetails: String?) {
        _selectedMotorcycle.value = Pair(motorcycleLicensePlate, motorcycleDetails)
        Log.d(tag, "Motorcycle selected: licensePlate=$motorcycleLicensePlate, details=$motorcycleDetails")
    }

    fun validateAndRegister(
        date: String,
        clientCI: String,
        motorcycleLicensePlate: String,
        reason: String
    ) {
        Log.d(tag, "Validating: date=$date, clientCI=$clientCI, motorcycleLicensePlate=$motorcycleLicensePlate, reason=$reason")
        // Limpiar errores previos
        _dateTimeError.value = null
        _clientError.value = null
        _motorcycleError.value = null
        _reasonError.value = null

        // Validar campos
        if (date.isEmpty()) {
            _dateTimeError.value = "La fecha y hora no pueden estar vacías"
        } else if (!date.matches(Regex("^\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2} (AM|PM)$"))) {
            _dateTimeError.value = "Formato inválido (ej. 28-03-2025 08:00 AM)"
        }

        if (clientCI.isEmpty()) {
            _clientError.value = "Debe seleccionar un cliente"
        } else if (!clientCI.matches(Regex("^\\d+$"))) {
            _clientError.value = "El CI del cliente debe contener solo números"
        }

        if (motorcycleLicensePlate.isEmpty()) {
            _motorcycleError.value = "Debe seleccionar una motocicleta"
        }

        // Verificar si todos los campos son válidos
        if (_dateTimeError.value == null &&
            _clientError.value == null &&
            _motorcycleError.value == null &&
            _reasonError.value == null
        ) {
            try {
                // Convertir fecha a formato ISO 8601
                val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val parsedDate = inputFormat.parse(date)
                val isoDate = outputFormat.format(parsedDate)

                val reception = ReceptionSend(
                    date = isoDate,
                    clientCI = clientCI.toInt(),
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = 10387210, // Hardcode
                    reasons = reasons.toList(),
                    images = photos.toList()
                )
                registerReception(reception)
            } catch (e: NumberFormatException) {
                _clientError.value = "El CI del cliente debe ser un número válido"
                Log.e(tag, "Invalid clientCI format: $clientCI", e)
            } catch (e: Exception) {
                _dateTimeError.value = "Error al procesar la fecha"
                Log.e(tag, "Date parsing error: $date", e)
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

    fun editReason(index: Int, newReason: String) {
        if (index in reasons.indices && newReason.isNotEmpty()) {
            reasons[index] = newReason
            _reasonsList.value = reasons.toList()
            Log.d(tag, "Reason edited at index $index: $newReason")
        }
    }

    fun deleteReason(index: Int) {
        if (index in reasons.indices) {
            val removed = reasons.removeAt(index)
            _reasonsList.value = reasons.toList()
            Log.d(tag, "Reason deleted at index $index: $removed")
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

    private fun registerReception(reception: ReceptionSend) {
        Log.d(tag, "Registering reception with data: " +
                "date=${reception.date}, " +
                "clientCI=${reception.clientCI}, " +
                "motorcycleLicensePlate=${reception.motorcycleLicensePlate}, " +
                "employeeCI=${reception.employeeCI}, " +
                "reasons=${reception.reasons?.joinToString()}, " +
                "images=${reception.images?.joinToString()}")
        repository.createReception(reception).enqueue(object : Callback<ReceptionSend> {
            override fun onResponse(call: Call<ReceptionSend>, response: Response<ReceptionSend>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Servicio registrado exitosamente"
                    Log.d(tag, "Reception registered successfully")
                    // Limpiar listas después de un registro exitoso
                    reasons.clear()
                    photos.clear()
                    _reasonsList.value = emptyList()
                    _photosCount.value = 0
                    _selectedDateTime.value = null
                } else {
                    val errorMsg = "Error al registrar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<ReceptionSend>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearSelections() {
        _selectedClient.value = Pair(null, null)
        _selectedMotorcycle.value = Pair(null, null)
        _selectedDateTime.value = null
        reasons.clear()
        photos.clear()
        _reasonsList.value = emptyList()
        _photosCount.value = 0
        Log.d(tag, "Selections cleared")
    }
}