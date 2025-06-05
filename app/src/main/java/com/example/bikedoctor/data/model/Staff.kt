package com.example.bikedoctor.data.model

data class Staff (
    val id: String?,
    val name: String,
    val lastName: String,
    val ci: Int,
    val password: String,
    val age: Int,
    val numberPhone: Int,
    val role: String
)

data class StaffPost (
    val name: String,
    val lastName: String,
    val ci: Int,
    val password: String,
    val age: Int,
    val numberPhone: Int,
    val role: String
)

data class StaffLogin (
    val ci: Int,
    val password: String,
)

data class StaffChangePassword (
    val ci: Int,
    val currentPassword: String,
    val newPassword: String
)

data class StaffResetPassword (
    val ci: Int,
    val numberPhone: Int,
    val newPassword: String,
)