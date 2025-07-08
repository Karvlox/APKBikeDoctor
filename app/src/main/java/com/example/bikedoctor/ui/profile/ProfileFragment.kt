package com.example.bikedoctor.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.repository.DeliveryRepository
import com.example.bikedoctor.ui.main.SessionViewModel
import com.example.bikedoctor.ui.signIn.SignIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {

    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val deliveryRepository = DeliveryRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Referencias a los TextView
        val fullNameText = view.findViewById<TextView>(R.id.fullNameText)
        val ciText = view.findViewById<TextView>(R.id.ciText)
        val ageText = view.findViewById<TextView>(R.id.ageText)
        val phoneText = view.findViewById<TextView>(R.id.phoneText)
        val historyListView = view.findViewById<ListView>(R.id.historyListView)

        // Observar el token y extraer datos
        sessionViewModel.token.observe(viewLifecycleOwner) { token ->
            if (token != null) {
                try {
                    // Decodificar el payload del JWT
                    val payload = token.split(".")[1]
                    val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
                    val decodedPayload = String(decodedBytes, Charsets.UTF_8)
                    val jsonPayload = JSONObject(decodedPayload)

                    // Extraer datos
                    val name = jsonPayload.getString("Name")
                    val lastName = jsonPayload.getString("LastName")
                    val ci = jsonPayload.getString("Ci")
                    val age = jsonPayload.getString("Age")
                    val phone = jsonPayload.getString("NumberPhone")

                    // Establecer los valores en los TextView
                    fullNameText.text = "Nombre: $name $lastName"
                    ciText.text = "CI: $ci"
                    ageText.text = "Edad: $age"
                    phoneText.text = "Número de Teléfono: $phone"

                    // Cargar historial usando el CI
                    loadHistory(ci.toInt(), historyListView)
                } catch (e: Exception) {
                    fullNameText.text = "Nombre: No disponible"
                    ciText.text = "CI: No disponible"
                    ageText.text = "Edad: No disponible"
                    phoneText.text = "Número de Teléfono: No disponible"
                    Toast.makeText(requireContext(), "Error al decodificar el token: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                fullNameText.text = "Nombre: No disponible"
                ciText.text = "CI: No disponible"
                ageText.text = "Edad: No disponible"
                phoneText.text = "Número de Teléfono: No disponible"
                startActivity(Intent(requireContext(), SignIn::class.java))
                requireActivity().finish()
            }
        }

        return view
    }

    private fun loadHistory(employeeCi: Int, listView: ListView) {
        CoroutineScope(Dispatchers.Main).launch {
            deliveryRepository.getDeliveriesByEmployee(employeeCi, pageNumber = 1, pageSize = 10)
                .enqueue(object : Callback<List<Delivery>> {
                    override fun onResponse(call: Call<List<Delivery>>, response: Response<List<Delivery>>) {
                        if (response.isSuccessful) {
                            // Filtrar entregas con reviewed: true
                            val deliveries = response.body()?.filter { it.reviewed == true } ?: emptyList()
                            val adapter = DeliveryAdapter(requireContext(), deliveries)
                            listView.adapter = adapter
                            // Actualizar el conteo de trabajos completados
                            view?.findViewById<TextView>(R.id.completedJobsCount)?.text = deliveries.size.toString()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error al cargar historial: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<List<Delivery>>, t: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            "Error de red: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }
}