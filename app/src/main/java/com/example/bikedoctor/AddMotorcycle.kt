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
import java.util.zip.Inflater


class AddMotorcycle : Fragment() {

    private lateinit var ciInput: TextInputEditText
    private lateinit var marcaMotocicletaInput: TextInputEditText
    private lateinit var modeloMotocicletaInput: TextInputEditText
    private lateinit var matriculaInput: TextInputEditText
    private lateinit var colorInput: TextInputEditText

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

        ciInput = view.findViewById(R.id.inputCiCliente)
        marcaMotocicletaInput = view.findViewById(R.id.inputMarcaMotocicleta)
        modeloMotocicletaInput = view.findViewById(R.id.inputModeloMotocicleta)
        matriculaInput = view.findViewById(R.id.inputMatricula)
        colorInput = view.findViewById(R.id.inputColor)

        view.findViewById<View>(R.id.buttomRegisterMotocicleta).setOnClickListener {
            registerMotorcycle()
        }

        view.findViewById<View>(R.id.button_cancel).setOnClickListener {
            clearFields()
            parentFragmentManager.popBackStack()
        }
        return view
    }

    private fun registerMotorcycle() {
        val clientCI = ciInput.text.toString().trim()
        val brand = marcaMotocicletaInput.text.toString().trim()
        val model = modeloMotocicletaInput.text.toString().trim()
        val licensePlateNumber = matriculaInput.text.toString().trim()
        val color = colorInput.text.toString().trim()

        if (clientCI.isEmpty() || brand.isEmpty() || model.isEmpty()
            || licensePlateNumber.isEmpty() || color.isEmpty()) {
            Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val motorcycle = Motorcycle(
            clientCI = clientCI.toInt(),
            brand = brand,
            model = model,
            licensePlateNumber = licensePlateNumber,
            color = color
            )

            motorcycleApi.createMotorcycle(motorcycle).enqueue(object : Callback<Motorcycle> {
                override fun onResponse(call: Call<Motorcycle>, response: Response<Motorcycle>) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Motocicleta registrada exitosamente", Toast.LENGTH_SHORT).show()
                        clearFields()
                    } else {
                        Toast.makeText(context, "Error al registrar: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Motorcycle?>, t: Throwable) {
                    Toast.makeText(context, "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "CI y Matricula deben ser válidos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        ciInput.text?.clear()
        marcaMotocicletaInput.text?.clear()
        modeloMotocicletaInput.text?.clear()
        matriculaInput.text?.clear()
        colorInput.text?.clear()
    }
}