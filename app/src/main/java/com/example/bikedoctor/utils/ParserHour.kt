package com.example.bikedoctor.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ParserHour {

    public fun parserHourService(date: String?): String {
        return try {
            // Verificar si date es nulo
            if (date == null) {
                val calendar = Calendar.getInstance()
                val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                outputFormat.timeZone = TimeZone.getTimeZone("UTC")
                return outputFormat.format(calendar.time)
            }

            // Parsear la fecha de reception.date (formato ISO 8601)
            val inputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val newDate = inputFormat.parse(date) ?: throw Exception("Fecha nula después de parsear")
            
            // Formatear la fecha al formato deseado
            val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(newDate)

        } catch (e: Exception) {
            // Manejar cualquier excepción (como ParseException)
            android.util.Log.e("DateParser", "Error al parsear la fecha: ${e.message}")
            // Devolver la fecha actual en formato ISO 8601
            val calendar = Calendar.getInstance()
            val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
            outputFormat.timeZone = TimeZone.getTimeZone("UTC")
            outputFormat.format(calendar.time)
        }
    }
}