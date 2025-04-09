package com.example.bikedoctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class Home : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Referencias a los CardView
        val cardAddClient = view.findViewById<CardView>(R.id.card_add_client)
        val cardAddMotorcycle = view.findViewById<CardView>(R.id.card_add_motorcycle)

        // Navegar a AddClient
        cardAddClient.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, AddClient())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Navegar a AddMotorcycle
        cardAddMotorcycle.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, AddMotorcycle())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
}
