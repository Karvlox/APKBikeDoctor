package com.example.bikedoctor.ui.service

import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Control
import com.example.bikedoctor.data.model.QualityControlPost
import com.example.bikedoctor.data.repository.ControlRepository
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ControlFormViewModel : ViewModel() {

    private val repository = ControlRepository()
    private val controls = mutableListOf<Control>()
    private val tag = "ControlFormViewModel"
    private var controlId: String? = null
    private var token: String? = null

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

    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> = _registerStatus

    private val _controlsList = MutableLiveData<List<Control>>()
    val controlsList: LiveData<List<Control>> = _controlsList

    private val _selectedDateTime = MutableLiveData<String?>()
    val selectedDateTime: LiveData<String?> = _selectedDateTime

    private val _selectedClient = MutableLiveData<String?>()
    val selectedClient: LiveData<String?> = _selectedClient

    private val _selectedMotorcycle = MutableLiveData<String?>()
    val selectedMotorcycle: LiveData<String?> = _selectedMotorcycle

    private val _reviewed = MutableLiveData<Boolean?>()
    val reviewed: LiveData<Boolean?> = _reviewed

    fun setToken(token: String?) {
        this.token = token
        Log.d(tag, "Token set: $token")
    }

    fun setDateTime(dateTime: String?) {
        _selectedDateTime.value = dateTime
        Log.d(tag, "DateTime set: $dateTime")
    }

    fun initializeControl(
        id: String?,
        date: String?,
        clientCI: String?,
        motorcycleLicensePlate: String?,
        employeeCI: String?,
        listControls: List<Control>?,
        reviewed: Boolean?
    ) {
        controlId = id
        _selectedDateTime.value = date
        _selectedClient.value = clientCI
        _selectedMotorcycle.value = motorcycleLicensePlate
        this.controls.clear()
        if (listControls != null) {
            this.controls.addAll(listControls)
        }
        _controlsList.value = this.controls.toList()
        _reviewed.value = reviewed
        Log.d(tag, "Initialized control: id=$id, date=$date, clientCI=$clientCI, motorcycleLicensePlate=$motorcycleLicensePlate")
    }

    fun validateAndRegister(
        date: String,
        clientCI: String,
        motorcycleLicensePlate: String,
        nameControl: String,
        detailControl: String,
        token: String?
    ) {
        Log.d(tag, "Validating: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, nameControl=$nameControl, detailControl=$detailControl")
        _dateTimeError.value = null
        _clientError.value = null
        _motorcycleError.value = null
        _errorDiagnosticError.value = null
        _errorDetailError.value = null

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

        if (controls.isEmpty() && nameControl.isEmpty()) {
            _errorDiagnosticError.value = "Debe agregar al menos un control"
        }

        if (nameControl.isNotEmpty() || detailControl.isNotEmpty()) {
            if (nameControl.isEmpty()) {
                _errorDiagnosticError.value = "El nombre no puede estar vacío"
            }
            if (detailControl.isEmpty()) {
                _errorDetailError.value = "La descripción no puede estar vacía"
            }
        }

        var employeeCI: Int? = null
        if (token == null) {
            _clientError.value = "No se encontró el token de autenticación"
            Log.e(tag, "No token provided")
            return
        }

        try {
            val payload = token.split(".")[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedPayload = String(decodedBytes, Charsets.UTF_8)
            val jsonPayload = JSONObject(decodedPayload)
            employeeCI = jsonPayload.getString("Ci").toIntOrNull()
            if (employeeCI == null) {
                _clientError.value = "El CI del empleado no es válido"
                Log.e(tag, "Invalid employee CI in token")
                return
            }
        } catch (e: Exception) {
            _clientError.value = "Error al decodificar el token"
            Log.e(tag, "Token decoding error: ${e.message}", e)
            return
        }

        if (_dateTimeError.value == null &&
            _clientError.value == null &&
            _motorcycleError.value == null &&
            _errorDiagnosticError.value == null &&
            _errorDetailError.value == null
        ) {
            try {
                val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val parsedDate = inputFormat.parse(date)
                val isoDate = outputFormat.format(parsedDate)

                if (nameControl.isNotEmpty() && detailControl.isNotEmpty()) {
                    controls.add(Control(nameControl, detailControl))
                    _controlsList.value = controls.toList()
                }

                val control = QualityControlPost(
                    date = isoDate,
                    clientCI = clientCI.toInt(),
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = employeeCI,
                    listControls = controls.toList(),
                    reviewed = _reviewed.value ?: false
                )

                if (controlId == null) {
                    createControl(control)
                } else {
                    updateControl(controlId!!, control)
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

    fun addControl(name: String, detail: String) {
        if (name.isNotEmpty() && detail.isNotEmpty()) {
            controls.add(Control(name, detail))
            _controlsList.value = controls.toList()
            Log.d(tag, "Control added: name=$name, detail=$detail")
        } else {
            if (name.isEmpty()) _errorDiagnosticError.value = "El nombre no puede estar vacío"
            if (detail.isEmpty()) _errorDetailError.value = "La descripción no puede estar vacía"
        }
    }

    fun editControl(index: Int, newName: String, newDetail: String) {
        if (index in controls.indices && newName.isNotEmpty() && newDetail.isNotEmpty()) {
            controls[index] = Control(newName, newDetail)
            _controlsList.value = controls.toList()
            Log.d(tag, "Control edited at index $index: name=$newName, detail=$newDetail")
        } else {
            _registerStatus.value = "Todos los campos deben estar completos"
        }
    }

    fun deleteControl(index: Int) {
        if (index in controls.indices) {
            val removed = controls.removeAt(index)
            _controlsList.value = controls.toList()
            Log.d(tag, "Control deleted at index $index: $removed")
        }
    }

    private fun createControl(control: QualityControlPost) {
        Log.d(tag, "Creating control: $control")
        repository.createControls(control).enqueue(object : Callback<QualityControlPost> {
            override fun onResponse(call: Call<QualityControlPost>, response: Response<QualityControlPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Control registrado exitosamente"
                    Log.d(tag, "Control created successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al registrar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<QualityControlPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    private fun updateControl(id: String, control: QualityControlPost) {
        Log.d(tag, "Updating control with id=$id: $control")
        repository.updateControls(id, control).enqueue(object : Callback<QualityControlPost> {
            override fun onResponse(call: Call<QualityControlPost>, response: Response<QualityControlPost>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Control actualizado exitosamente"
                    Log.d(tag, "Control updated successfully")
                    clearSelections()
                } else {
                    val errorMsg = "Error al actualizar: ${response.code()} ${response.message()}"
                    _registerStatus.value = errorMsg
                    Log.e(tag, errorMsg)
                }
            }

            override fun onFailure(call: Call<QualityControlPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                _registerStatus.value = errorMsg
                Log.e(tag, errorMsg, t)
            }
        })
    }

    fun clearSelections() {
        controlId = null
        token = null
        _selectedClient.value = null
        _selectedMotorcycle.value = null
        _selectedDateTime.value = null
        _reviewed.value = null
        controls.clear()
        _controlsList.value = emptyList()
        Log.d(tag, "Selections cleared")
    }
}