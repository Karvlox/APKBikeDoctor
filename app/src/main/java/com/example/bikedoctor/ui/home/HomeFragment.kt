package com.example.bikedoctor.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.ui.client.AddClientFragment
import com.example.bikedoctor.ui.main.SessionViewModel
import com.example.bikedoctor.ui.motorcycle.AddMotorcycleFragment
import com.example.bikedoctor.ui.service.ReceptionFormFragment
import com.example.bikedoctor.ui.signIn.SignIn
import org.json.JSONObject

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Referencias a los CardView
        val cardAddClient = view.findViewById<CardView>(R.id.card_add_client)
        val cardAddMotorcycle = view.findViewById<CardView>(R.id.card_add_motorcycle)
        val cardNewRepair = view.findViewById<CardView>(R.id.new_repair)

        // Referencias a los TextView para datos dinámicos
        val welcomeText = view.findViewById<TextView>(R.id.welcome_text)
        val pendingJobsText = view.findViewById<TextView>(R.id.numero_de_trabajos)

        // Navegar a AddClient
        cardAddClient.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, AddClientFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Navegar a AddMotorcycle
        cardAddMotorcycle.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, AddMotorcycleFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Navegar a AddService
        cardNewRepair.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, ReceptionFormFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Observar datos dinámicos de HomeViewModel
        viewModel.homeData.observe(viewLifecycleOwner) { homeData ->
            pendingJobsText.text = homeData.pendingJobsCount.toString()
        }

        // Observar el token y extraer el nombre
        sessionViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                try {
                    // Decodificar el payload del JWT
                    val payload = token.split(".")[1] // El payload es la segunda parte del JWT
                    val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
                    val decodedPayload = String(decodedBytes, Charsets.UTF_8)
                    val jsonPayload = JSONObject(decodedPayload)
                    val userName = jsonPayload.getString("Nombre") // Extraer el campo "Nombre"
                    welcomeText.text = "Bienvenido de nuevo $userName"
                } catch (e: Exception) {
                    welcomeText.text = "Bienvenido de nuevo"
                    Toast.makeText(requireContext(), "Error al decodificar el token", Toast.LENGTH_SHORT).show()
                }
            } else {
                welcomeText.text = "Bienvenido de nuevo"
                startActivity(Intent(requireContext(), SignIn::class.java))
                requireActivity().finish()
            }
        }

        return view
    }
}