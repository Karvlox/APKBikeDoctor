package com.example.bikedoctor.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Delivery
import java.text.SimpleDateFormat
import java.util.Locale

class DeliveryAdapter(
    context: Context,
    private val deliveries: List<Delivery>
) : ArrayAdapter<Delivery>(context, 0, deliveries) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.list_item_story, parent, false
        )

        val delivery = deliveries[position]

        val idNumberText = view.findViewById<TextView>(R.id.id_number)
        val motorcycleText = view.findViewById<TextView>(R.id.name_motorcicle)
        val dateText = view.findViewById<TextView>(R.id.date)

        val id = delivery.id ?: "Sin ID"
        idNumberText.text = if (id.length > 15) "#${id.substring(0, 15)}..." else "#$id"

        motorcycleText.text = "Motocicleta: ${delivery.motorcycleLicensePlate ?: "Sin datos"}"

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = delivery.date?.let { inputFormat.parse(it) }
            dateText.text = parsedDate?.let { outputFormat.format(it) } ?: "Sin fecha"
        } catch (e: Exception) {
            dateText.text = delivery.date ?: "Sin fecha"
        }

        return view
    }
}