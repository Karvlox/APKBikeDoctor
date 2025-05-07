package com.example.bikedoctor.ui.motorcycle

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Motorcycle

class MotorcycleAdapter(context: Context, motorcycles: List<Motorcycle>) :
    ArrayAdapter<Motorcycle>(context, 0, motorcycles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_motorcycle, parent, false)

        val motorcycle = getItem(position) ?: return view

        val modelText = view.findViewById<TextView>(R.id.motorcycle_model)
        val plateText = view.findViewById<TextView>(R.id.motorcycle_plate)

        modelText.text = motorcycle.model
        plateText.text = "Placa: ${motorcycle.licensePlateNumber}"

        return view
    }
}