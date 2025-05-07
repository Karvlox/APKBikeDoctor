package com.example.bikedoctor.ui.service

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Diagnosis

class DiagnosisAdapter(context: Context, diagnosis: List<Diagnosis>) :
    ArrayAdapter<Diagnosis>(context, 0, diagnosis) {

    private val tag = "DiagnosisAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val diagnosis = getItem(position)
        if (diagnosis == null) {
            Log.e(tag, "Diagnostic at position $position is null")
            return view
        }

        Log.d(tag, "Rendering diagnostic: id=${diagnosis.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = diagnosis.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${diagnosis.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${diagnosis.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${diagnosis.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Lista de Diagnostico: ${diagnosis.listDiagnostic?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.imageView14)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${diagnosis.id}")
            // TODO: Implementar acción de edición
        }
        view.findViewById<ImageView>(R.id.imageView16)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${diagnosis.id}")
            // TODO: Implementar acción de continuación
        }

        return view
    }
}