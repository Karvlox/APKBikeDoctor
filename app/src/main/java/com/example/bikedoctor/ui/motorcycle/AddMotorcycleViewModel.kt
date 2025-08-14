package com.example.bikedoctor.ui.motorcycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Motorcycle
import com.example.bikedoctor.data.repository.MotorcycleRepository
import com.example.bikedoctor.utils.MotorcycleValidator

class AddMotorcycleViewModel : ViewModel() {

    private val repository = MotorcycleRepository()
    var isEditing: Boolean = false

    // LiveData para los errores de validación
    private val _ciError = MutableLiveData<String?>()
    val ciError: LiveData<String?> = _ciError

    private val _brandError = MutableLiveData<String?>()
    val brandError: LiveData<String?> = _brandError

    private val _modelError = MutableLiveData<String?>()
    val modelError: LiveData<String?> = _modelError

    private val _licensePlateError = MutableLiveData<String?>()
    val licensePlateError: LiveData<String?> = _licensePlateError

    private val _colorError = MutableLiveData<String?>()
    val colorError: LiveData<String?> = _colorError

    // LiveData para el estado del registro
    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> = _registerStatus

    fun validateAndRegister(
        clientCI: String,
        brand: String,
        model: String,
        licensePlate: String,
        color: String
    ) {
        // Limpiar errores previos
        _ciError.value = null
        _brandError.value = null
        _modelError.value = null
        _licensePlateError.value = null
        _colorError.value = null

        // Validar los campos
        _ciError.value = MotorcycleValidator.validateClientCI(clientCI)
        _brandError.value = MotorcycleValidator.validateBrand(brand)
        _modelError.value = MotorcycleValidator.validateModel(model)
        _licensePlateError.value = MotorcycleValidator.validateLicensePlate(licensePlate)
        _colorError.value = MotorcycleValidator.validateColor(color)

        // Verificar si todos los campos son válidos
        if (MotorcycleValidator.validateMotorcycle(clientCI, brand, model, licensePlate, color)) {
            try {
                val motorcycle = Motorcycle(
                    clientCI = clientCI.toInt(),
                    brand = brand,
                    model = model,
                    licensePlateNumber = licensePlate,
                    color = color
                )

                if (isEditing) {
                    updateMotorcycle(motorcycle)
                } else {
                    registerMotorcycle(motorcycle)
                }
            } catch (e: NumberFormatException) {
                _ciError.value = "La cédula debe ser un número válido"
            }
        }
    }

    private fun registerMotorcycle(motorcycle: Motorcycle) {
        repository.createMotorcycle(motorcycle).enqueue(object : retrofit2.Callback<Motorcycle> {
            override fun onResponse(call: retrofit2.Call<Motorcycle>, response: retrofit2.Response<Motorcycle>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Motocicleta registrada exitosamente"
                } else {
                    _registerStatus.value = "Error al registrar: ${response.message()}"
                }
            }

            override fun onFailure(call: retrofit2.Call<Motorcycle>, t: Throwable) {
                _registerStatus.value = "Error de conexión: ${t.message}"
            }
        })
    }

    private fun updateMotorcycle(motorcycle: Motorcycle) {
        repository.updateMotorcycle(motorcycle).enqueue(object : retrofit2.Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: retrofit2.Response<Void>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Motocicleta actualizada exitosamente"
                } else {
                    _registerStatus.value = "Error al actualizar: ${response.message()}"
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                _registerStatus.value = "Error de conexión: ${t.message}"
            }
        })
    }
}