package com.example.bikedoctor.utils

object MotorcycleValidator {
    fun validateClientCI(ci: String): String? {
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

    fun validateBrand(brand: String): String? {
        if (brand.isEmpty()) {
            return "La marca no puede estar vacía"
        }
        if (brand.length > 50) {
            return "La marca no puede tener más de 50 caracteres"
        }
        return null
    }

    fun validateModel(model: String): String? {
        if (model.isEmpty()) {
            return "El modelo no puede estar vacío"
        }
        if (model.length > 50) {
            return "El modelo no puede tener más de 50 caracteres"
        }
        return null
    }

    fun validateLicensePlate(licensePlate: String): String? {
        if (licensePlate.isEmpty()) {
            return "La matrícula no puede estar vacía"
        }
        if (!licensePlate.matches(Regex("^\\d{4}[A-Z]{3}$"))) {
            return "La matrícula debe tener 4 números seguidos de 3 letras mayúsculas (ej. 1234ABC)"
        }
        return null
    }

    fun validateColor(color: String): String? {
        if (color.isEmpty()) {
            return "El color no puede estar vacío"
        }
        if (color.length > 30) {
            return "El color no puede tener más de 30 caracteres"
        }
        return null
    }

    fun validateMotorcycle(
        clientCI: String,
        brand: String,
        model: String,
        licensePlate: String,
        color: String
    ): Boolean {
        var isValid = true

        validateClientCI(clientCI)?.let { isValid = false }
        validateBrand(brand)?.let { isValid = false }
        validateModel(model)?.let { isValid = false }
        validateLicensePlate(licensePlate)?.let { isValid = false }
        validateColor(color)?.let { isValid = false }

        return isValid
    }
}