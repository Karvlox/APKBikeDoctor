package com.example.bikedoctor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class AddClient : Fragment() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var lastNameInput: TextInputEditText
    private lateinit var ciInput: TextInputEditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var genderInput: TextInputEditText

    data class Client(
        val ci: Int,
        val name: String,
        val lastName: String,
        val age: Int = 25,
        val numberPhone: Int,
        val gender: String
    )

    interface ClientApi {
        @POST("api/Client")
        fun createClient(@Body client: Client): Call<Client>
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://bikedoctor-production.up.railway.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val clientApi = retrofit.create(ClientApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_client, container, false)

        nameInput = view.findViewById(R.id.textInputEditText)
        lastNameInput = view.findViewById(R.id.textInputEditText2)
        ciInput = view.findViewById(R.id.textInputEditText3)
        phoneInput = view.findViewById(R.id.textInputEditText4)
        genderInput = view.findViewById(R.id.textInputEditText5)

        view.findViewById<View>(R.id.buttom_register_client).setOnClickListener {
            registerClient()
        }

        // Botón Cancelar: limpia campos y regresa al fragmento anterior
        view.findViewById<View>(R.id.button_cancel).setOnClickListener {
            clearFields()
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun registerClient() {
        Toast.makeText(context, "Botón de registro presionado", Toast.LENGTH_SHORT).show()

        val name = nameInput.text.toString().trim()
        val lastName = lastNameInput.text.toString().trim()
        val ci = ciInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val gender = genderInput.text.toString().trim().uppercase()

        if (name.isEmpty() || lastName.isEmpty() || ci.isEmpty() || phone.isEmpty() || gender.isEmpty()) {
            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val client = Client(
                ci = ci.toInt(),
                name = name,
                lastName = lastName,
                numberPhone = phone.toInt(),
                gender = gender
            )

            clientApi.createClient(client).enqueue(object : Callback<Client> {
                override fun onResponse(call: Call<Client>, response: Response<Client>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Cliente registrado exitosamente", Toast.LENGTH_SHORT).show()
                        clearFields()
                    } else {
                        Toast.makeText(context, "Error al registrar: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Client>, t: Throwable) {
                    Toast.makeText(context, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "CI y Teléfono deben ser números válidos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        nameInput.text?.clear()
        lastNameInput.text?.clear()
        ciInput.text?.clear()
        phoneInput.text?.clear()
        genderInput.text?.clear()
    }
}