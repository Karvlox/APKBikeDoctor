package com.example.bikedoctor.data.model

data class Service(
    val dateTime: String, // Formato: "28-03-2025 08:00 AM"
    val clientId: Int,
    val motorcycleId: String, // Usamos licensePlateNumber como ID
    val reasons: List<String>,
    val photos: List<String> // URLs o nombres de archivo
)