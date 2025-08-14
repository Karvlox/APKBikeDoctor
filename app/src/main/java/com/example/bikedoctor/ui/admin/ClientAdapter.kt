package com.example.bikedoctor.ui.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Client

class ClientAdapter(
    context: Context,
    private val clients: List<Client>,
    private val onActionClick: (Client, String) -> Unit
) : ArrayAdapter<Client>(context, R.layout.list_item_admin, clients) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_admin, parent, false)
        val client = clients[position]

        val nameTextView = view.findViewById<TextView>(R.id.name)
        val dataTextView = view.findViewById<TextView>(R.id.data)
        val editButton = view.findViewById<ImageView>(R.id.edit)
        val deleteButton = view.findViewById<ImageView>(R.id.delete)

        nameTextView.text = "${client.name} ${client.lastName}"
        dataTextView.text = client.ci.toString()

        editButton.setOnClickListener { onActionClick(client, "edit") }
        deleteButton.setOnClickListener { onActionClick(client, "delete") }

        return view
    }
}