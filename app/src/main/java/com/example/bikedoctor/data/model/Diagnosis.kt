package com.example.bikedoctor.data.model

data class Diagnosis(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listDiagnostic: List<Diagnostic>?,
)

data class Diagnostic(
    val error: String?,
    val detailOfError: String?,
    val timeSpent: Int?
)