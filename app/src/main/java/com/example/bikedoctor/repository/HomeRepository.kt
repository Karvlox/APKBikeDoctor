package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.HomeData

class HomeRepository {
    // Simulación de datos; reemplazar con llamada a API o base de datos
    fun getHomeData(): HomeData {
        return HomeData(
            userName = "Usuario", // Podría venir de SharedPreferences o API
            pendingJobsCount = 1 // Podría venir de una API de servicios
        )
    }
}