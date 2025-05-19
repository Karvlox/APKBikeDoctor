package com.example.bikedoctor.data.model

data class Reception(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val reasons: List<String>?,
    val images: List<String>?,
    val reviewed: Boolean?
)

data class ReceptionPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val reasons: List<String>?,
    val images: List<String>?,
    val reviewed: Boolean?
)

data class ReceptionChangeStatus(
    val reviewed: Boolean
)