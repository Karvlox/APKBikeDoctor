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
import com.example.bikedoctor.data.model.QualityControl

class ControlAdapter(context: Context, control: List<QualityControl>) :
    ArrayAdapter<QualityControl>(context, 0, control) {

    private val tag = "ControlAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val control = getItem(position)
        if (control == null) {
            Log.e(tag, "Control at position $position is null")
            return view
        }

        Log.d(tag, "Rendering control: id=${control.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = control.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${control.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${control.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${control.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Lista de Controles: ${control.listControls?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.imageView14)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${control.id}")
            // TODO: Implementar acción de edición
        }
        view.findViewById<ImageView>(R.id.imageView16)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${control.id}")
            // TODO: Implementar acción de continuación
        }

        return view
    }
}