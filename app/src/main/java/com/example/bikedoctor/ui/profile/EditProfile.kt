package com.example.bikedoctor.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Staff
import com.example.bikedoctor.data.model.UpdateStaffRequest
import com.example.bikedoctor.data.repository.StaffRepository
import com.example.bikedoctor.ui.main.SessionViewModel
import com.example.bikedoctor.ui.signIn.SignIn
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfile : Fragment() {

    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val staffRepository = StaffRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        // Referencias a los elementos de la UI
        val backButton = view.findViewById<ImageView>(R.id.backButtom)
        val nameEdit = view.findViewById<TextInputEditText>(R.id.textInputNameEdit)
        val lastNameEdit = view.findViewById<TextInputEditText>(R.id.textInputLastNameEdit)
        val ciEdit = view.findViewById<TextInputEditText>(R.id.textInputCiEdit)
        val ageEdit = view.findViewById<TextInputEditText>(R.id.textInputAgeEdit)
        val numberPhoneEdit = view.findViewById<TextInputEditText>(R.id.textInputNumberPhoneEdit)
        val roleSpinner = view.findViewById<Spinner>(R.id.filterSpinner)
        val saveButton = view.findViewById<TextView>(R.id.button_save)
        val cancelButton = view.findViewById<TextView>(R.id.button_cancel)

        // Configurar el Spinner con roles
        val roles = arrayOf("ADMIN", "EMPLOYEE") // Ajusta según los roles disponibles
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        // Obtener el ID del argumento
        val staffId = arguments?.getString("staff_id")
        if (staffId == null) {
            Toast.makeText(requireContext(), "Error: ID de usuario no disponible", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
            return view
        }

        // Obtener datos actuales del usuario
        staffRepository.getStaffById(staffId).enqueue(object : Callback<Staff> {
            override fun onResponse(call: Call<Staff>, response: Response<Staff>) {
                if (response.isSuccessful) {
                    response.body()?.let { staff ->
                        nameEdit.setText(staff.name)
                        lastNameEdit.setText(staff.lastName)
                        ciEdit.setText(staff.ci.toString())
                        ageEdit.setText(staff.age.toString())
                        numberPhoneEdit.setText(staff.numberPhone.toString())
                        val rolePosition = roles.indexOf(staff.role)
                        if (rolePosition >= 0) {
                            roleSpinner.setSelection(rolePosition)
                        }
                    } ?: run {
                        Toast.makeText(requireContext(), "Error: No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al obtener datos: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Staff>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Configurar botón Atrás
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Configurar botón Cancelar
        cancelButton.setOnClickListener {
            // Limpiar los campos
            nameEdit.text?.clear()
            lastNameEdit.text?.clear()
            ciEdit.text?.clear()
            ageEdit.text?.clear()
            numberPhoneEdit.text?.clear()
            roleSpinner.setSelection(0)
            // Regresar al fragmento anterior
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Configurar botón Guardar
        saveButton.setOnClickListener {
            val name = nameEdit.text.toString().trim()
            val lastName = lastNameEdit.text.toString().trim()
            val ci = ciEdit.text.toString().toIntOrNull()
            val age = ageEdit.text.toString().toIntOrNull()
            val numberPhone = numberPhoneEdit.text.toString().toIntOrNull()
            val role = roleSpinner.selectedItem.toString()

            // Validar campos
            if (name.isEmpty() || lastName.isEmpty() || ci == null || age == null || numberPhone == null) {
                Toast.makeText(requireContext(), "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateRequest = UpdateStaffRequest(
                name = name,
                lastName = lastName,
                ci = ci,
                age = age,
                numberPhone = numberPhone,
                role = role
            )

            staffRepository.updateStaff(staffId, updateRequest).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                        // Cerrar sesión
                        sessionViewModel.clearToken()
                        startActivity(Intent(requireContext(), SignIn::class.java))
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), "Error al actualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error de red: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }
}