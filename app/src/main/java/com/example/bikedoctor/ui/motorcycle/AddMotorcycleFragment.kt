package com.example.bikedoctor.ui.motorcycle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.google.android.material.textfield.TextInputLayout

class AddMotorcycleFragment : Fragment() {

    private val viewModel: AddMotorcycleViewModel by viewModels()

    private lateinit var ciInputLayout: TextInputLayout
    private lateinit var brandInputLayout: TextInputLayout
    private lateinit var modelInputLayout: TextInputLayout
    private lateinit var licensePlateInputLayout: TextInputLayout
    private lateinit var colorInputLayout: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_motorcycle, container, false)

        // Inicializar los TextInputLayouts
        ciInputLayout = view.findViewById(R.id.ciInputLayout)
        brandInputLayout = view.findViewById(R.id.brandInputLayout)
        modelInputLayout = view.findViewById(R.id.modelInputLayout)
        licensePlateInputLayout = view.findViewById(R.id.licensePlateInputLayout)
        colorInputLayout = view.findViewById(R.id.colorInputLayout)

        // Botones
        view.findViewById<View>(R.id.buttomRegisterMotocicleta).setOnClickListener {
            val clientCI = ciInputLayout.editText?.text.toString().trim()
            val brand = brandInputLayout.editText?.text.toString().trim()
            val model = modelInputLayout.editText?.text.toString().trim()
            val licensePlate = licensePlateInputLayout.editText?.text.toString().trim()
            val color = colorInputLayout.editText?.text.toString().trim()

            viewModel.validateAndRegister(clientCI, brand, model, licensePlate, color)
        }

        view.findViewById<View>(R.id.button_cancel).setOnClickListener {
            clearFields()
            parentFragmentManager.popBackStack()
        }

        // Observar los errores y el estado del registro
        viewModel.ciError.observe(viewLifecycleOwner) { error ->
            ciInputLayout.error = error
        }
        viewModel.brandError.observe(viewLifecycleOwner) { error ->
            brandInputLayout.error = error
        }
        viewModel.modelError.observe(viewLifecycleOwner) { error ->
            modelInputLayout.error = error
        }
        viewModel.licensePlateError.observe(viewLifecycleOwner) { error ->
            licensePlateInputLayout.error = error
        }
        viewModel.colorError.observe(viewLifecycleOwner) { error ->
            colorInputLayout.error = error
        }

        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Motocicleta registrada")) {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }

        return view
    }

    private fun clearFields() {
        ciInputLayout.editText?.text?.clear()
        brandInputLayout.editText?.text?.clear()
        modelInputLayout.editText?.text?.clear()
        licensePlateInputLayout.editText?.text?.clear()
        colorInputLayout.editText?.text?.clear()
    }
}