package com.example.bikedoctor.data.model

data class Repair(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listReparations: List<Reparation>?,
    val reviewed: Boolean?
)

data class Reparation(
    val nameReparation: String?,
    val descriptionReparation: String?
)