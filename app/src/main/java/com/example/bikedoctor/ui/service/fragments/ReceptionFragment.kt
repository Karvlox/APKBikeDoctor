package com.example.bikedoctor.ui.service.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.ui.main.SessionViewModel
import com.example.bikedoctor.ui.service.ReceptionAdapter
import com.example.bikedoctor.ui.service.ReceptionViewModel
import com.example.bikedoctor.ui.signIn.SignIn

class ReceptionFragment : Fragment() {

    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val viewModel: ReceptionViewModel by viewModels()
    private val tag = "ReceptionFragment"
    private var currentToken: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_reception layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_reception, container, false)
        } catch (e: Exception) {
            Log.e(tag, "Error inflating layout: ${e.message}", e)
            return null
        }

        Log.d(tag, "Initializing views")
        val listView = view.findViewById<ListView>(R.id.receptions_list_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_reception)
        val errorText = view.findViewById<TextView>(R.id.error_text_reception)
        val servicesNumberText = view.findViewById<TextView>(R.id.servicesNumber_reception)

        if (listView == null || progressBar == null || errorText == null || servicesNumberText == null) {
            Log.e(tag, "One or more views are null")
            return view
        }

        sessionViewModel.token.observe(viewLifecycleOwner) { token ->
            Log.d(tag, "Token observed: $token")
            currentToken = token
            if (token == null) {
                Log.e(tag, "No token, redirecting to SignIn")
                Toast.makeText(requireContext(), "Sesión no iniciada", Toast.LENGTH_LONG).show()
                startActivity(Intent(requireContext(), SignIn::class.java))
                requireActivity().finish()
            } else {
                viewModel.fetchReceptions(1, 100, token)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(tag, "isLoading: $isLoading")
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Log.e(tag, "Error: $error")
                errorText.text = error
                errorText.visibility = View.VISIBLE
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            } else {
                errorText.visibility = View.GONE
            }
        }

        viewModel.receptions.observe(viewLifecycleOwner) { receptions ->
            Log.d(tag, "Receptions updated: ${receptions.size}")
            val adapter = ReceptionAdapter(requireContext(), receptions, viewModel, currentToken)
            listView.adapter = adapter
            servicesNumberText.text = "${receptions.size} Servicios"
        }

        return view
    }
}