package com.example.bikedoctor.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.repository.ClientRepository
import com.example.bikedoctor.ui.client.AddClientFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientManagement : Fragment() {

    private val clientRepository = ClientRepository()
    private val viewModel: ClientManagementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_client_management, container, false)

        val listView = view.findViewById<ListView>(R.id.client_list)
        val backButton = view.findViewById<ImageView>(R.id.back_buttom)

        // Configurar el botón de retroceso
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Observar la lista de clientes
        viewModel.clients.observe(viewLifecycleOwner) { clients ->
            val adapter = ClientAdapter(requireContext(), clients) { client, action ->
                when (action) {
                    "edit" -> {
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frame_layout, AddClientFragment.newInstance(client))
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    "delete" -> {
                        clientRepository.deleteClient(client.ci).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(requireContext(), "Cliente eliminado", Toast.LENGTH_SHORT).show()
                                    viewModel.loadClients()
                                } else {
                                    Toast.makeText(requireContext(), "Error al eliminar cliente", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                }
            }
            listView.adapter = adapter
        }

        return view
    }
}