package com.example.bikedoctor.ui.service

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.LaborCost

class CostApprovalAdapterList(
    private val costApproval: List<LaborCost>,
    private val onEdit: (Int, LaborCost) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<CostApprovalAdapterList.CostApprovalViewHolder>() {

    class CostApprovalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val errorText: TextView = itemView.findViewById(R.id.error_text)
        val errorDetailText: TextView = itemView.findViewById(R.id.error_detail_text)
        val timeSpentText: TextView = itemView.findViewById(R.id.time_spent_text)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CostApprovalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_details, parent, false)
        return CostApprovalViewHolder(view)
    }

    override fun onBindViewHolder(holder: CostApprovalViewHolder, position: Int) {
        val sparePart = costApproval[position]
        holder.errorText.text = sparePart.nameProduct ?: "Sin Repuesto"
        holder.errorDetailText.text = sparePart.descriptionProduct ?: "Sin descripción"
        holder.timeSpentText.text = "Tiempo: ${sparePart.price ?: 0} minutos"

        holder.editButton.setOnClickListener {
            onEdit(position, sparePart)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(position)
        }
    }

    override fun getItemCount(): Int = costApproval.size
}