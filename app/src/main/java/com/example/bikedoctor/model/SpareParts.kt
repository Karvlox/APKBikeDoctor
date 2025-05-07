package com.example.bikedoctor.data.model

data class SpareParts(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val clientName: String?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listSpareParts: List<SparePart>?,
)

data class SparePart(
    val nameSparePart: String?,
    val detailSparePart: String?,
    val price: Int?
)