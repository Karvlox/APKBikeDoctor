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
import com.example.bikedoctor.data.model.Repair

class RepairAdapter(context: Context, repair: List<Repair>) :
    ArrayAdapter<Repair>(context, 0, repair) {

    private val tag = "RepairAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val repair = getItem(position)
        if (repair == null) {
            Log.e(tag, "Repairs at position $position is null")
            return view
        }

        Log.d(tag, "Rendering repairs: id=${repair.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = repair.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${repair.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${repair.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${repair.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Lista de Reparaciones: ${repair.listReparations?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${repair.id}")
            // TODO: Implementar acción de edición
        }
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${repair.id}")
            // TODO: Implementar acción de continuación
        }

        return view
    }
}