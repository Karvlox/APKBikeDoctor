package com.example.bikedoctor.ui.client

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.repository.ClientRepository
import com.example.bikedoctor.utils.ClientValidator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddClientViewModel : ViewModel() {

    private val repository = ClientRepository()

    // LiveData para los errores de validación
    private val _nameError = MutableLiveData<String?>()
    val nameError: LiveData<String?> = _nameError

    private val _lastNameError = MutableLiveData<String?>()
    val lastNameError: LiveData<String?> = _lastNameError

    private val _ciError = MutableLiveData<String?>()
    val ciError: LiveData<String?> = _ciError

    private val _ageError = MutableLiveData<String?>()
    val ageError: LiveData<String?> = _ageError

    private val _phoneError = MutableLiveData<String?>()
    val phoneError: LiveData<String?> = _phoneError

    private val _genderError = MutableLiveData<String?>()
    val genderError: LiveData<String?> = _genderError

    // LiveData para el estado del registro
    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> = _registerStatus

    fun validateAndRegister(
        name: String,
        lastName: String,
        ci: String,
        age: String,
        phone: String,
        gender: String
    ) {
        // Limpiar errores previos
        _nameError.value = null
        _lastNameError.value = null
        _ciError.value = null
        _ageError.value = null
        _phoneError.value = null
        _genderError.value = null

        // Validar los campos
        _nameError.value = ClientValidator.validateName(name)
        _lastNameError.value = ClientValidator.validateLastName(lastName)
        _ciError.value = ClientValidator.validateCI(ci)
        _ageError.value = ClientValidator.validateAge(age)
        _phoneError.value = ClientValidator.validatePhone(phone)
        _genderError.value = ClientValidator.validateGender(gender)

        // Verificar si todos los campos son válidos
        if (ClientValidator.validateClient(name, lastName, ci, age, phone, gender)) {
            val client = Client(
                ci = ci.toInt(),
                name = name,
                lastName = lastName,
                age = age.toInt(),
                numberPhone = phone.toInt(),
                gender = gender
            )
            registerClient(client)
        }
    }

    private fun registerClient(client: Client) {
        repository.registerClient(client).enqueue(object : Callback<Client> {
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                if (response.isSuccessful) {
                    _registerStatus.value = "Cliente registrado con éxito"
                } else {
                    _registerStatus.value = "Error: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                _registerStatus.value = "Error de red: ${t.message}"
            }
        })
    }
}