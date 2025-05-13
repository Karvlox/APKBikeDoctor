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
import com.example.bikedoctor.data.model.CostApproval

class CostApprovalAdapter(context: Context, costApproval: List<CostApproval>) :
    ArrayAdapter<CostApproval>(context, 0, costApproval) {

    private val tag = "CostApprovalAdapter"

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val costApproval = getItem(position)
        if (costApproval == null) {
            Log.e(tag, "Cost Approval at position $position is null")
            return view
        }

        Log.d(tag, "Rendering Cost Approval: id=${costApproval.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = costApproval.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${costApproval.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${costApproval.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${costApproval.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Lista de Aprobacion de Costos: ${costApproval.listLaborCosts?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${costApproval.id}")
            // TODO: Implementar acción de edición
        }
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${costApproval.id}")
            // TODO: Implementar acción de continuación
        }

        return view
    }
}