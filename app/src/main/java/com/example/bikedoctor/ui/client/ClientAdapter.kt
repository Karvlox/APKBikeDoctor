package com.example.bikedoctor.ui.client

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Client

class ClientAdapter(context: Context, clients: List<Client>) :
    ArrayAdapter<Client>(context, 0, clients) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_client, parent, false)

        val client = getItem(position) ?: return view

        val nameTextView = view.findViewById<TextView>(R.id.client_name)
        val ciTextView = view.findViewById<TextView>(R.id.client_ci)

        nameTextView.text = "${client.name} ${client.lastName}"
        ciTextView.text = "CI: ${client.ci}"

        return view
    }
}