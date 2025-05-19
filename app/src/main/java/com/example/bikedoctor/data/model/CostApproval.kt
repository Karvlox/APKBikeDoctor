package com.example.bikedoctor.data.model

data class CostApproval(
    val id: String?,
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listLaborCosts: List<LaborCost>?,
    val reviewed: Boolean?
)

data class LaborCost(
    val nameProduct: String?,
    val descriptionProduct: String?,
    val price: String?
)

data class CostApprovalPost(
    val date: String?,
    val clientCI: Int?,
    val motorcycleLicensePlate: String?,
    val employeeCI: Int?,
    val listLaborCosts: List<LaborCost>?,
    val reviewed: Boolean?
)