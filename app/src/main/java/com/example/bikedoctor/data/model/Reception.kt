package com.example.bikedoctor.data.model

data class Reception(
    val id: String?,
    val date: String?, // Formato: "28-03-2025 08:00 AM"
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val reasons: List<String>?,
    val images: List<String>?
)