package com.example.bikedoctor.ui.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Reparation

class RepairAdapterList(
    private val control: List<Reparation>,
    private val onEdit: (Int, Reparation) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<RepairAdapterList.RepairViewHolder>() {

    class RepairViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val errorText: TextView = itemView.findViewById(R.id.error_text)
        val errorDetailText: TextView = itemView.findViewById(R.id.error_detail_text)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepairViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_details, parent, false)
        return RepairViewHolder(view)
    }

    override fun onBindViewHolder(holder: RepairViewHolder, position: Int) {
        val qualityControl = control[position]
        holder.errorText.text = qualityControl.nameReparation ?: "Sin Titulo"
        holder.errorDetailText.text = qualityControl.descriptionReparation ?: "Sin descripción"

        holder.editButton.setOnClickListener {
            onEdit(position, qualityControl)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = control.size
}