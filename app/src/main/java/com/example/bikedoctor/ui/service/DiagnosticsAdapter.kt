package com.example.bikedoctor.ui.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Diagnostic

class DiagnosticsAdapter(
    private val diagnostics: List<Diagnostic>,
    private val onEdit: (Int, Diagnostic) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<DiagnosticsAdapter.DiagnosticViewHolder>() {

    class DiagnosticViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val errorText: TextView = itemView.findViewById(R.id.error_text)
        val errorDetailText: TextView = itemView.findViewById(R.id.error_detail_text)
        val timeSpentText: TextView = itemView.findViewById(R.id.time_spent_text)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiagnosticViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diagnostic, parent, false)
        return DiagnosticViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiagnosticViewHolder, position: Int) {
        val diagnostic = diagnostics[position]
        holder.errorText.text = diagnostic.error ?: "Sin error"
        holder.errorDetailText.text = diagnostic.detailOfError ?: "Sin descripción"
        holder.timeSpentText.text = "Tiempo: ${diagnostic.timeSpent ?: 0} minutos"

        holder.editButton.setOnClickListener {
            onEdit(position, diagnostic)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = diagnostics.size
}