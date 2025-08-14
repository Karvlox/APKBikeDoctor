package com.example.bikedoctor.ui.client

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R

class ClientsListFragment : Fragment() {

    private val viewModel: ClientsListViewModel by viewModels()
    private val tag = "ClientsListFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_clients_list, container, false)

        // Inicializar vistas
        val listView = view.findViewById<ListView>(R.id.clients_list_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        val errorText = view.findViewById<TextView>(R.id.error_text)
        val backButton = view.findViewById<ImageView>(R.id.back_button)

        // Botón de retroceso
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                errorText.text = error
                errorText.visibility = View.VISIBLE
                viewModel.clearError()
            } else {
                errorText.visibility = View.GONE
            }
        }

        // Observar lista de clientes
        viewModel.clients.observe(viewLifecycleOwner) { clients ->
            val adapter = ClientAdapter(requireContext(), clients)
            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedClient = clients[position]
                val clientId = selectedClient.ci?.toString() ?: ""
                val clientName = "${selectedClient.name} ${selectedClient.lastName}"
                Log.d(tag, "Sending client selection: client_id=$clientId, client_name=$clientName")
                setFragmentResult(
                    "client_selection",
                    bundleOf(
                        "client_id" to clientId,
                        "client_name" to clientName
                    )
                )
                parentFragmentManager.popBackStack()
            }
        }

        return view
    }
}