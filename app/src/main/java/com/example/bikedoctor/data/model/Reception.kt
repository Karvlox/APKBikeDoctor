package com.example.bikedoctor.data.model

data class Reception(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val reasons: List<String>?,
    val images: List<String>?
)

data class ReceptionSend(
    val date: String?, // Formato esperado por el backend: "2025-05-08T03:30:56.278Z"
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val reasons: List<String>?,
    val images: List<String>?
)