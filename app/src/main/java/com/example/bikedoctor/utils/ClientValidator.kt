package com.example.bikedoctor.utils

object ClientValidator {
    fun validateName(name: String): String? {
        if (name.isEmpty()) {
            return "El nombre no puede estar vacío"
        }
        if (name.length > 50) {
            return "El nombre no puede tener más de 50 caracteres"
        }
        if (!name.matches(Regex("^[a-zA-Z\\s]*$"))) {
            return "El nombre solo puede contener letras y espacios"
        }
        return null
    }

    fun validateLastName(lastName: String): String? {
        if (lastName.isEmpty()) {
            return "El apellido no puede estar vacío"
        }
        if (lastName.length > 50) {
            return "El apellido no puede tener más de 50 caracteres"
        }
        if (!lastName.matches(Regex("^[a-zA-Z\\s]*$"))) {
            return "El apellido solo puede contener letras y espacios"
        }
        return null
    }

    fun validateCI(ci: String): String? {
        if (ci.isEmpty()) {
            return "La cédula no puede estar vacía"
        }
        if (!ci.matches(Regex("^[0-9]*$"))) {
            return "La cédula solo puede contener números"
        }
        if (ci.length < 7 || ci.length > 8) {
            return "La cédula debe tener entre 7 y 8 dígitos"
        }
        return null
    }

    fun validateAge(age: String): String? {
        if (age.isEmpty()) {
            return "La edad no puede estar vacía"
        }
        if (!age.matches(Regex("^[0-9]*$"))) {
            return "La edad solo puede contener números"
        }
        val ageInt = age.toIntOrNull() ?: return "La edad debe ser un número válido"
        if (ageInt < 16 || ageInt > 70) {
            return "La edad debe estar entre 16 y 70 años"
        }
        return null
    }

    fun validatePhone(phone: String): String? {
        if (phone.isEmpty()) {
            return "El número de teléfono no puede estar vacío"
        }
        if (!phone.matches(Regex("^[0-9]*$"))) {
            return "El número de teléfono solo puede contener números"
        }
        if (phone.length != 8) {
            return "El número de teléfono debe tener exactamente 8 dígitos"
        }
        return null
    }

    fun validateGender(gender: String): String? {
        if (gender.isEmpty()) {
            return "El género no puede estar vacío"
        }
        if (gender != "MASCULINO" && gender != "FEMENINO") {
            return "El género debe ser MASCULINO o FEMENINO"
        }
        return null
    }

    fun validateClient(
        name: String,
        lastName: String,
        ci: String,
        age: String,
        phone: String,
        gender: String
    ): Boolean {
        var isValid = true

        validateName(name)?.let { isValid = false }
        validateLastName(lastName)?.let { isValid = false }
        validateCI(ci)?.let { isValid = false }
        validateAge(age)?.let { isValid = false }
        validatePhone(phone)?.let { isValid = false }
        validateGender(gender)?.let { isValid = false }

        return isValid
    }
}