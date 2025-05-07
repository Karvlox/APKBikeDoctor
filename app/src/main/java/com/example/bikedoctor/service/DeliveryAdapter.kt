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
import com.example.bikedoctor.data.model.Delivery

class DeliveryAdapter(context: Context, delivery: List<Delivery>) :
    ArrayAdapter<Delivery>(context, 0, delivery) {

    private val tag = "DeliveryAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val delivery = getItem(position)
        if (delivery == null) {
            Log.e(tag, "Delivery at position $position is null")
            return view
        }

        Log.d(tag, "Rendering delivery: id=${delivery.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = delivery.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${delivery.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${delivery.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${delivery.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Formulario llenado?: ${delivery.surveyCompleted?: "Sin confirmación"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.imageView14)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${delivery.id}")
            // TODO: Implementar acción de edición
        }
        view.findViewById<ImageView>(R.id.imageView16)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${delivery.id}")
            // TODO: Implementar acción de continuación
        }

        return view
    }
}