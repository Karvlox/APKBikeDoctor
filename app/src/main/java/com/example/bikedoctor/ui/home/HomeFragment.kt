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
import com.example.bikedoctor.ui.admin.ClientManagement
import com.example.bikedoctor.ui.admin.MotorcycleManagement
import com.example.bikedoctor.ui.client.AddClientFragment
import com.example.bikedoctor.ui.main.SessionViewModel
import com.example.bikedoctor.ui.motorcycle.AddMotorcycleFragment
import com.example.bikedoctor.ui.service.ReceptionFormFragment
import com.example.bikedoctor.ui.service.TableWorkFragment
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

        val cardPendingJobs = view.findViewById<CardView>(R.id.trabajos_pendientes_)
        val cardAddClient = view.findViewById<CardView>(R.id.card_add_client)
        val cardAddMotorcycle = view.findViewById<CardView>(R.id.card_add_motorcycle)
        val cardNewRepair = view.findViewById<CardView>(R.id.new_repair)
        val clientsButton = view.findViewById<CardView>(R.id.clients_buttom)
        val motorcycleButton = view.findViewById<CardView>(R.id.motorcycle_buttom)
        val textPendingJobs = view.findViewById<TextView>(R.id.trabajos_pendientes)

        val welcomeText = view.findViewById<TextView>(R.id.welcome_text)

        cardPendingJobs.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, TableWorkFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        cardAddClient.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, AddClientFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        cardAddMotorcycle.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, AddMotorcycleFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        cardNewRepair.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, ReceptionFormFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        clientsButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, ClientManagement())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        motorcycleButton.setOnClickListener {
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, MotorcycleManagement())
            transaction.addToBackStack(null)
            transaction.commit()
        }

        sessionViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                try {
                    val payload = token.split(".")[1]
                    val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
                    val decodedPayload = String(decodedBytes, Charsets.UTF_8)
                    val jsonPayload = JSONObject(decodedPayload)
                    val userName = jsonPayload.getString("Name")
                    val lastName = jsonPayload.getString("LastName")
                    val role = jsonPayload.getString("Role")
                    welcomeText.text = "Bienvenido de nuevo $userName $lastName"

                    if (role == "ADMIN") {
                        clientsButton.visibility = View.VISIBLE
                        motorcycleButton.visibility = View.VISIBLE
                        textPendingJobs.text = "Ver trabajos pendientes"
                    } else {
                        clientsButton.visibility = View.GONE
                        motorcycleButton.visibility = View.GONE
                        textPendingJobs.text = "Trabajos Asignados"
                    }
                } catch (e: Exception) {
                    welcomeText.text = "Bienvenido de nuevo"
                    clientsButton.visibility = View.GONE
                    motorcycleButton.visibility = View.GONE
                    textPendingJobs.text = "Trabajos Asignados"
                    Toast.makeText(requireContext(), "Error al decodificar el token: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                welcomeText.text = "Bienvenido de nuevo"
                clientsButton.visibility = View.GONE
                motorcycleButton.visibility = View.GONE
                textPendingJobs.text = "Trabajos Asignados"
                startActivity(Intent(requireContext(), SignIn::class.java))
                requireActivity().finish()
            }
        }

        return view
    }
}