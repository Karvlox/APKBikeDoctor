package com.example.bikedoctor.data.model

data class Delivery(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val surveyCompleted: Boolean?,
    val reviewed: Boolean?
)

data class DeliveryPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val surveyCompleted: Boolean? = false,
    val reviewed: Boolean? = false
)