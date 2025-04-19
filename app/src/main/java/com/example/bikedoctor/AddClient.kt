package com.example.bikedoctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bikedoctor.utils.ClientValidator
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class AddClient : Fragment() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var lastNameInputLayout: TextInputLayout
    private lateinit var ciInputLayout: TextInputLayout
    private lateinit var ageInputLayout: TextInputLayout
    private lateinit var phoneInputLayout: TextInputLayout
    private lateinit var genderInputLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_client, container, false)

        // Inicializar los TextInputLayouts
        nameInputLayout = view.findViewById(R.id.nameInputLayout)
        lastNameInputLayout = view.findViewById(R.id.lastNameInputLayout)
        ciInputLayout = view.findViewById(R.id.ciInputLayout)
        ageInputLayout = view.findViewById(R.id.ageInputLayout)
        phoneInputLayout = view.findViewById(R.id.phoneInputLayout)
        genderInputLayout = view.findViewById(R.id.genderInputLayout)

        // Botones
        val cancelButton = view.findViewById<View>(R.id.button_cancel)
        val registerButton = view.findViewById<View>(R.id.button_register_client)

        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        registerButton.setOnClickListener {
            validateAndRegister()
        }

        return view
    }

    private fun validateAndRegister() {
        nameInputLayout.error = null
        lastNameInputLayout.error = null
        ciInputLayout.error = null
        ageInputLayout.error = null
        phoneInputLayout.error = null
        genderInputLayout.error = null

        val name = nameInputLayout.editText?.text.toString().trim()
        val lastName = lastNameInputLayout.editText?.text.toString().trim()
        val ci = ciInputLayout.editText?.text.toString().trim()
        val age = ageInputLayout.editText?.text.toString().trim()
        val phone = phoneInputLayout.editText?.text.toString().trim()
        val gender = genderInputLayout.editText?.text.toString().trim()

        ClientValidator.validateName(name)?.let { nameInputLayout.error = it }
        ClientValidator.validateLastName(lastName)?.let { lastNameInputLayout.error = it }
        ClientValidator.validateCI(ci)?.let { ciInputLayout.error = it }
        ClientValidator.validateAge(age)?.let { ageInputLayout.error = it }
        ClientValidator.validatePhone(phone)?.let { phoneInputLayout.error = it }
        ClientValidator.validateGender(gender)?.let { genderInputLayout.error = it }

        if (ClientValidator.validateClient(name, lastName, ci, age, phone, gender)) {
            val client = Client(
                ci = ci.toInt(),
                name = name,
                lastName = lastName,
                age = age.toInt(),
                numberPhone = phone.toInt(),
                gender = gender
            )

            registerClient(client)
        }
    }

    private fun registerClient(client: Client) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://bikedoctor-production.up.railway.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ClientApi::class.java)
        api.registerClient(client).enqueue(object : Callback<Client> {
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Cliente registrado con éxito", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                Toast.makeText(context, "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

data class Client(
    val ci: Int,
    val name: String,
    val lastName: String,
    val age: Int,
    val numberPhone: Int,
    val gender: String
)

interface ClientApi {
    @POST("api/Client")
    fun registerClient(@Body client: Client): Call<Client>
}