package com.example.bikedoctor.ui.motorcycle

import android.os.Bundle
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

class MotorcycleListFragment : Fragment() {

    private val viewModel: MotorcycleListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_motorcycles_list, container, false)

        // Inicializar vistas
        val listView = view.findViewById<ListView>(R.id.motorcycles_list_view)
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

        // Observar lista de motocicletas
        viewModel.motorcycles.observe(viewLifecycleOwner) { motorcycles ->
            val adapter = MotorcycleAdapter(requireContext(), motorcycles)
            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedMotorcycle = motorcycles[position]
                // Devolver la motocicleta seleccionada a AddServiceFragment
                setFragmentResult(
                    "motorcycle_selection",
                    bundleOf(
                        "motorcycle_id" to selectedMotorcycle.model,
                        "motorcycle_details" to selectedMotorcycle.licensePlateNumber
                    )
                )
                parentFragmentManager.popBackStack()
            }
        }

        return view
    }
}