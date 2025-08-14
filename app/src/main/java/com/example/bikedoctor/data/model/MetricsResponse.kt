package com.example.bikedoctor.data.model

import com.google.gson.annotations.SerializedName

data class MetricsResponse(
    @SerializedName("totalFormularios") val totalFormularios: Int = 0,
    @SerializedName("porcentajeSatisfechos") val porcentajeSatisfechos: Int = 0,
    @SerializedName("quejasRepetidas") val quejasRepetidas: List<MetricItem>? = emptyList(),
    @SerializedName("aspectosRepetidos") val aspectosRepetidos: List<MetricItem>? = emptyList(),
    @SerializedName("total") val totalReparaciones: Int = 0,
    @SerializedName("terminados") val terminados: Int = 0,
    @SerializedName("noTerminados") val noTerminados: Int = 0,
    @SerializedName("porcentajeTerminados") val porcentajeTerminados: Int = 0,
    @SerializedName("conFormulario") val conFormulario: Int = 0,
    @SerializedName("porcentajeFormulario") val porcentajeFormulario: Int = 0,
    @SerializedName("motivosMasRepetidos") val motivosRepetidos: List<MetricItem>? = emptyList() // Generic field for both phrases and words
)

data class MetricItem(
    @SerializedName("texto") val texto: String,
    @SerializedName("conteo") val conteo: Int
)