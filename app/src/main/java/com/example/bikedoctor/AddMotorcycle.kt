package com.example.bikedoctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bikedoctor.utils.MotorcycleValidator
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class AddMotorcycle : Fragment() {

    private lateinit var ciInputLayout: TextInputLayout
    private lateinit var brandInputLayout: TextInputLayout
    private lateinit var modelInputLayout: TextInputLayout
    private lateinit var licensePlateInputLayout: TextInputLayout
    private lateinit var colorInputLayout: TextInputLayout

    data class Motorcycle(
        val clientCI: Int,
        val brand: String,
        val model: String,
        val licensePlateNumber: String,
        val color: String
    )

    interface MotorcycleApi {
        @POST("api/Motorcycle")
        fun createMotorcycle(@Body motorcycle: Motorcycle): Call<Motorcycle>
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://bikedoctor-production.up.railway.app/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val motorcycleApi = retrofit.create(MotorcycleApi::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_motorcycle, container, false)

        ciInputLayout = view.findViewById(R.id.ciInputLayout)
        brandInputLayout = view.findViewById(R.id.brandInputLayout)
        modelInputLayout = view.findViewById(R.id.modelInputLayout)
        licensePlateInputLayout = view.findViewById(R.id.licensePlateInputLayout)
        colorInputLayout = view.findViewById(R.id.colorInputLayout)

        view.findViewById<View>(R.id.buttomRegisterMotocicleta).setOnClickListener {
            validateAndRegister()
        }

        view.findViewById<View>(R.id.button_cancel).setOnClickListener {
            clearFields()
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun validateAndRegister() {
        ciInputLayout.error = null
        brandInputLayout.error = null
        modelInputLayout.error = null
        licensePlateInputLayout.error = null
        colorInputLayout.error = null

        val clientCI = ciInputLayout.editText?.text.toString().trim()
        val brand = brandInputLayout.editText?.text.toString().trim()
        val model = modelInputLayout.editText?.text.toString().trim()
        val licensePlate = licensePlateInputLayout.editText?.text.toString().trim()
        val color = colorInputLayout.editText?.text.toString().trim()

        // Validar
        MotorcycleValidator.validateClientCI(clientCI)?.let { ciInputLayout.error = it }
        MotorcycleValidator.validateBrand(brand)?.let { brandInputLayout.error = it }
        MotorcycleValidator.validateModel(model)?.let { modelInputLayout.error = it }
        MotorcycleValidator.validateLicensePlate(licensePlate)?.let { licensePlateInputLayout.error = it }
        MotorcycleValidator.validateColor(color)?.let { colorInputLayout.error = it }

        if (MotorcycleValidator.validateMotorcycle(clientCI, brand, model, licensePlate, color)) {
            try {
                val motorcycle = Motorcycle(
                    clientCI = clientCI.toInt(),
                    brand = brand,
                    model = model,
                    licensePlateNumber = licensePlate,
                    color = color
                )
                registerMotorcycle(motorcycle)
            } catch (e: NumberFormatException) {
                ciInputLayout.error = "La cédula debe ser un número válido"
            }
        }
    }

    private fun registerMotorcycle(motorcycle: Motorcycle) {
        motorcycleApi.createMotorcycle(motorcycle).enqueue(object : Callback<Motorcycle> {
            override fun onResponse(call: Call<Motorcycle>, response: Response<Motorcycle>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Motocicleta registrada exitosamente", Toast.LENGTH_SHORT).show()
                    clearFields()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(context, "Error al registrar: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Motorcycle>, t: Throwable) {
                Toast.makeText(context, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearFields() {
        ciInputLayout.editText?.text?.clear()
        brandInputLayout.editText?.text?.clear()
        modelInputLayout.editText?.text?.clear()
        licensePlateInputLayout.editText?.text?.clear()
        colorInputLayout.editText?.text?.clear()
    }
}