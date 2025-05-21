package com.example.bikedoctor.ui.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.SparePart

class SparePartsAdapterList(
    private val spareParts: List<SparePart>,
    private val onEdit: (Int, SparePart) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<SparePartsAdapterList.SparePartsViewHolder>() {

    class SparePartsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val errorText: TextView = itemView.findViewById(R.id.error_text)
        val errorDetailText: TextView = itemView.findViewById(R.id.error_detail_text)
        val timeSpentText: TextView = itemView.findViewById(R.id.time_spent_text)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SparePartsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diagnostic, parent, false)
        return SparePartsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SparePartsViewHolder, position: Int) {
        val sparePart = spareParts[position]
        holder.errorText.text = sparePart.nameSparePart ?: "Sin error"
        holder.errorDetailText.text = sparePart.detailSparePart ?: "Sin descripción"
        holder.timeSpentText.text = "Tiempo: ${sparePart.price ?: 0} minutos"

        holder.editButton.setOnClickListener {
            onEdit(position, sparePart)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = spareParts.size
}
