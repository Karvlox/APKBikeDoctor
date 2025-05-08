package com.example.bikedoctor.ui.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R

class ReasonsAdapter(
    private val reasons: List<String>,
    private val onEdit: (Int, String) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<ReasonsAdapter.ReasonViewHolder>() {

    class ReasonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reasonText: TextView = itemView.findViewById(R.id.reason_text)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reason, parent, false)
        return ReasonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReasonViewHolder, position: Int) {
        val reason = reasons[position]
        holder.reasonText.text = reason

        holder.editButton.setOnClickListener {
            // Mostrar un diálogo para editar (implementado en AddServiceFragment)
            onEdit(position, reason)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = reasons.size
}