package com.example.bikedoctor.ui.service.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.ui.service.DiagnosisAdapter
import com.example.bikedoctor.ui.service.DiagnosisViewModel

class DiagnosticFragment : Fragment() {

    private val viewModel: DiagnosisViewModel by viewModels()
    private val tag = "DiagnosticFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_diagnostic layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_diagnostic, container, false)
        } catch (e: Exception) {
            Log.e(tag, "Error inflating layout: ${e.message}", e)
            return null
        }

        // Inicializar vistas
        Log.d(tag, "Initializing views")
        val listView = view.findViewById<ListView>(R.id.diagnosis_list_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_diagnosis)
        val errorText = view.findViewById<TextView>(R.id.error_text_diagnosis)
        val servicesNumberText = view.findViewById<TextView>(R.id.servicesNumber_diagnosis)

        if (listView == null || progressBar == null || errorText == null || servicesNumberText == null) {
            Log.e(tag, "One or more views are null")
            return view
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(tag, "isLoading: $isLoading")
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Log.e(tag, "Error: $error")
                errorText.text = error
                errorText.visibility = View.VISIBLE
                viewModel.clearError()
            } else {
                errorText.visibility = View.GONE
            }
        }

        // Observar lista de diagnosticos
        viewModel.diagnosis.observe(viewLifecycleOwner) { diagnosis ->
            Log.d(tag, "Diagnosis updated: ${diagnosis.size}")
            val adapter = DiagnosisAdapter(requireContext(), diagnosis)
            listView.adapter = adapter
            servicesNumberText.text = "${diagnosis.size} Servicios"
        }

        return view
    }
}