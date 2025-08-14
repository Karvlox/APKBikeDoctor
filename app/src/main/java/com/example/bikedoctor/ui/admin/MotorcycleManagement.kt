package com.example.bikedoctor.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.data.repository.MotorcycleRepository
import com.example.bikedoctor.ui.motorcycle.AddMotorcycleFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MotorcycleManagement : Fragment() {

    private val motorcycleRepository = MotorcycleRepository()
    private val viewModel: MotorcycleManagementViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_motorcycle_management, container, false)

        val listView = view.findViewById<ListView>(R.id.motorcycle_list)
        val backButton = view.findViewById<ImageView>(R.id.back_buttom)

        // Configurar el botón de retroceso
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Observar la lista de motocicletas
        viewModel.motorcycles.observe(viewLifecycleOwner) { motorcycles ->
            val adapter = MotorcycleAdapter(requireContext(), motorcycles) { motorcycle, action ->
                when (action) {
                    "edit" -> {
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.frame_layout, AddMotorcycleFragment.newInstance(motorcycle))
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                    "delete" -> {
                        motorcycleRepository.deleteMotorcycleByLicensePlate(motorcycle.licensePlateNumber).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Toast.makeText(requireContext(), "Motocicleta eliminada", Toast.LENGTH_SHORT).show()
                                    viewModel.loadMotorcycles()
                                } else {
                                    Toast.makeText(requireContext(), "Error al eliminar motocicleta", Toast.LENGTH_SHORT).show()
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