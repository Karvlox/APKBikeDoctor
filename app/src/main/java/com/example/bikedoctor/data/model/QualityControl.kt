package com.example.bikedoctor.data.model

data class QualityControl(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listControls: List<Control>?,
    val reviewed: Boolean?
)

data class Control(
    val controlName: String?,
    val detailsControl: String?,
)