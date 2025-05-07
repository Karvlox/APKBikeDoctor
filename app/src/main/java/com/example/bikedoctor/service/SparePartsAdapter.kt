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
import com.example.bikedoctor.data.model.SpareParts

class SparePartsAdapter(context: Context, spareParts: List<SpareParts>) :
    ArrayAdapter<SpareParts>(context, 0, spareParts) {

    private val tag = "SparePartsAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val spareParts = getItem(position)
        if (spareParts == null) {
            Log.e(tag, "SpareParts at position $position is null")
            return view
        }

        Log.d(tag, "Rendering spare parts: id=${spareParts.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = spareParts.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${spareParts.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${spareParts.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${spareParts.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Lista de Repuestos: ${spareParts.listSpareParts?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.imageView14)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${spareParts.id}")
            // TODO: Implementar acción de edición
        }
        view.findViewById<ImageView>(R.id.imageView16)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${spareParts.id}")
            // TODO: Implementar acción de continuación
        }

        return view
    }
}