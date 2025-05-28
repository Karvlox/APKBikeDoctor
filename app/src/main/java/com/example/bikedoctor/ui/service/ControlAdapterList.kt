package com.example.bikedoctor.ui.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Control

class ControlAdapterList(
    private val control: List<Control>,
    private val onEdit: (Int, Control) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ControlAdapterList.ControlViewHolder>() {

    class ControlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val errorText: TextView = itemView.findViewById(R.id.error_text)
        val errorDetailText: TextView = itemView.findViewById(R.id.error_detail_text)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControlViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_details, parent, false)
        return ControlViewHolder(view)
    }

    override fun onBindViewHolder(holder: ControlViewHolder, position: Int) {
        val qualityControl = control[position]
        holder.errorText.text = qualityControl.controlName ?: "Sin Titulo"
        holder.errorDetailText.text = qualityControl.detailsControl ?: "Sin descripción"

        holder.editButton.setOnClickListener {
            onEdit(position, qualityControl)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = control.size
}