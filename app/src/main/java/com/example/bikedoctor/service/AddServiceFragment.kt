package com.example.bikedoctor.ui.service

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.ui.client.ClientsListFragment
import com.example.bikedoctor.ui.motorcycle.MotorcycleListFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddServiceFragment : Fragment() {

    private val viewModel: AddServiceViewModel by viewModels()

    private lateinit var dateTimeInputLayout: TextInputLayout
    private lateinit var dateTimeEditText: TextInputEditText
    private lateinit var reasonInputLayout: TextInputLayout
    private lateinit var clientSelectText: TextView
    private lateinit var motorcycleSelectText: TextView
    private lateinit var photosCountText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_add_service, container, false)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al inflar el layout: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }

        // Inicializar vistas con manejo de errores
        try {
            dateTimeInputLayout = view.findViewById(R.id.date_time_input_layout)
                ?: throw IllegalStateException("date_time_input_layout no encontrado")
            dateTimeEditText = dateTimeInputLayout.editText as TextInputEditText
            reasonInputLayout = view.findViewById(R.id.reason_input_layout)
                ?: throw IllegalStateException("reason_input_layout no encontrado")
            clientSelectText = view.findViewById(R.id.textView11)
                ?: throw IllegalStateException("textView11 no encontrado")
            motorcycleSelectText = view.findViewById(R.id.textView13)
                ?: throw IllegalStateException("textView13 no encontrado")
            photosCountText = view.findViewById(R.id.textView19)
                ?: throw IllegalStateException("textView19 no encontrado")
        } catch (e: Exception) {
            Toast.makeText(context, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            return view
        }

        // Hacer el campo de fecha y hora no editable manualmente
        dateTimeEditText.isEnabled = true
        dateTimeEditText.keyListener = null // Deshabilitar entrada de texto

        // Configurar DatePicker y TimePicker
        dateTimeEditText.setOnClickListener { showDateTimePicker() }

        // Botón de retroceso
        view.findViewById<ImageView>(R.id.imageView3)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Botón Cancelar
        view.findViewById<TextView>(R.id.button_cancel)?.setOnClickListener {
            clearFields()
            parentFragmentManager.popBackStack()
        }

        // Botón Guardar
        view.findViewById<TextView>(R.id.button_register_service)?.setOnClickListener {
            val dateTime = dateTimeEditText.text.toString().trim()
            val clientId = clientSelectText.tag?.toString() ?: ""
            val motorcycleId = motorcycleSelectText.tag?.toString() ?: ""
            val reason = reasonInputLayout.editText?.text.toString().trim()

            viewModel.validateAndRegister(dateTime, clientId, motorcycleId, reason)
        }

        // Botón Agregar Motivo
        view.findViewById<TextView>(R.id.textView16)?.setOnClickListener {
            val reason = reasonInputLayout.editText?.text.toString().trim()
            viewModel.addReason(reason)
            reasonInputLayout.editText?.text?.clear()
        }

        // Selección de cliente (TextView)
        clientSelectText.setOnClickListener {
            navigateToClientsList()
        }

        // Selección de cliente (ImageView)
        view.findViewById<ImageView>(R.id.imageView5)?.setOnClickListener {
            navigateToClientsList()
        }

        // Recibir cliente seleccionado
        setFragmentResultListener("client_selection") { _, bundle ->
            val clientId = bundle.getString("client_id") ?: ""
            val clientName = bundle.getString("client_name") ?: "Cliente Seleccionado"
            clientSelectText.text = clientName
            clientSelectText.tag = clientId
        }

        // Selección de motocicleta (TextView)
        motorcycleSelectText.setOnClickListener {
            navigateToMotorcyclesList()
        }

        // Selección de motocicleta (ImageView)
        view.findViewById<ImageView>(R.id.imageView7)?.setOnClickListener {
            navigateToMotorcyclesList()
        }

        // Recibir motocicleta seleccionada
        setFragmentResultListener("motorcycle_selection") { _, bundle ->
            val motorcycleId = bundle.getString("motorcycle_id") ?: ""
            val motorcycleDetails = bundle.getString("motorcycle_details") ?: "Motocicleta Seleccionada"
            motorcycleSelectText.text = motorcycleDetails
            motorcycleSelectText.tag = motorcycleId
        }

        // Botón de cámara (simulado)
        view.findViewById<ImageView>(R.id.imageView8)?.setOnClickListener {
            // TODO: Implementar captura de foto con permisos
            viewModel.addPhoto("photo_uri_${System.currentTimeMillis()}")
        }

        // Botón de galería (simulado)
        view.findViewById<ImageView>(R.id.imageView9)?.setOnClickListener {
            // TODO: Implementar selección de foto con permisos
            viewModel.addPhoto("photo_uri_${System.currentTimeMillis()}")
        }

        // Observar errores y estado
        viewModel.dateTimeError.observe(viewLifecycleOwner) { error ->
            dateTimeInputLayout.error = error
        }
        viewModel.clientError.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
        viewModel.motorcycleError.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
        viewModel.reasonError.observe(viewLifecycleOwner) { error ->
            reasonInputLayout.error = error
        }
        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Servicio registrado")) {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }
        viewModel.reasonsList.observe(viewLifecycleOwner) { reasons ->
            // TODO: Mostrar lista de motivos en la UI
            Toast.makeText(context, "Motivos: ${reasons.size}", Toast.LENGTH_SHORT).show()
        }
        viewModel.photosCount.observe(viewLifecycleOwner) { count ->
            photosCountText.text = "Fotos Adjuntadas ($count)"
        }

        return view
    }

    private fun navigateToClientsList() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, ClientsListFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToMotorcyclesList() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, MotorcycleListFragment())
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Mostrar DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Actualizar calendario con la fecha seleccionada
                calendar.set(selectedYear, selectedMonth, selectedDay)

                // Mostrar TimePickerDialog después de seleccionar la fecha
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, selectedHour, selectedMinute ->
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)

                        // Formatear la fecha y hora seleccionadas
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
                        dateTimeEditText.setText(dateFormat.format(calendar.time))
                    },
                    hour,
                    minute,
                    false // Formato de 12 horas (AM/PM)
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun clearFields() {
        dateTimeEditText.text?.clear()
        reasonInputLayout.editText?.text?.clear()
        clientSelectText.text = "SELECCIONAR"
        clientSelectText.tag = null
        motorcycleSelectText.text = "SELECCIONAR"
        motorcycleSelectText.tag = null
    }
}