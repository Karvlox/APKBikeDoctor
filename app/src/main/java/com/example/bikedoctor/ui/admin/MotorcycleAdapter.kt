package com.example.bikedoctor.ui.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Motorcycle

class MotorcycleAdapter(
    context: Context,
    private val motorcycles: List<Motorcycle>,
    private val onActionClick: (Motorcycle, String) -> Unit
) : ArrayAdapter<Motorcycle>(context, R.layout.list_item_admin, motorcycles) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_admin, parent, false)
        val motorcycle = motorcycles[position]

        val nameTextView = view.findViewById<TextView>(R.id.name)
        val dataTextView = view.findViewById<TextView>(R.id.data)
        val editButton = view.findViewById<ImageView>(R.id.edit)
        val deleteButton = view.findViewById<ImageView>(R.id.delete)

        nameTextView.text = "${motorcycle.brand} ${motorcycle.model}"
        dataTextView.text = motorcycle.licensePlateNumber

        editButton.setOnClickListener { onActionClick(motorcycle, "edit") }
        deleteButton.setOnClickListener { onActionClick(motorcycle, "delete") }

        return view
    }
}