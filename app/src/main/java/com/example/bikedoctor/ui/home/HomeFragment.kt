package com.example.bikedoctor.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.ui.client.AddClientFragment
import com.example.bikedoctor.ui.motorcycle.AddMotorcycleFragment
import com.example.bikedoctor.ui.service.ReceptionFormFragment

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

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

        // Navegar a AddService (asumimos que será reestructurado como AddServiceFragment)
        cardNewRepair.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, ReceptionFormFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Observar datos dinámicos
        viewModel.homeData.observe(viewLifecycleOwner) { homeData ->
            welcomeText.text = "Bienvenido de nuevo ${homeData.userName}"
            pendingJobsText.text = homeData.pendingJobsCount.toString()
        }

        return view
    }
}