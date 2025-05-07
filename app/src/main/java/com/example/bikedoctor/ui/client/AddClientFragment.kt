package com.example.bikedoctor.ui.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.google.android.material.textfield.TextInputLayout

class AddClientFragment : Fragment() {

    private val viewModel: AddClientViewModel by viewModels()

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
            val name = nameInputLayout.editText?.text.toString().trim()
            val lastName = lastNameInputLayout.editText?.text.toString().trim()
            val ci = ciInputLayout.editText?.text.toString().trim()
            val age = ageInputLayout.editText?.text.toString().trim()
            val phone = phoneInputLayout.editText?.text.toString().trim()
            val gender = genderInputLayout.editText?.text.toString().trim()

            viewModel.validateAndRegister(name, lastName, ci, age, phone, gender)
        }

        // Observar los errores y el estado del registro
        viewModel.nameError.observe(viewLifecycleOwner) { error ->
            nameInputLayout.error = error
        }
        viewModel.lastNameError.observe(viewLifecycleOwner) { error ->
            lastNameInputLayout.error = error
        }
        viewModel.ciError.observe(viewLifecycleOwner) { error ->
            ciInputLayout.error = error
        }
        viewModel.ageError.observe(viewLifecycleOwner) { error ->
            ageInputLayout.error = error
        }
        viewModel.phoneError.observe(viewLifecycleOwner) { error ->
            phoneInputLayout.error = error
        }
        viewModel.genderError.observe(viewLifecycleOwner) { error ->
            genderInputLayout.error = error
        }

        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Cliente registrado")) {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }

        return view
    }
}