package com.example.bikedoctor.ui.service

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private val tag = "AddServiceFragment"

    private lateinit var dateTimeInputLayout: TextInputLayout
    private lateinit var dateTimeEditText: TextInputEditText
    private lateinit var reasonInputLayout: TextInputLayout
    private lateinit var clientSelectText: TextView
    private lateinit var motorcycleSelectText: TextView
    private lateinit var photosCountText: TextView
    private lateinit var reasonsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_add_service layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_add_service, container, false)
        } catch (e: Exception) {
            Log.e(tag, "Error inflating layout: ${e.message}", e)
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
            reasonsRecyclerView = view.findViewById(R.id.reasons_recycler_view)
                ?: throw IllegalStateException("reasons_recycler_view no encontrado")
        } catch (e: Exception) {
            Log.e(tag, "Error initializing views: ${e.message}", e)
            Toast.makeText(context, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            return view
        }

        // Hacer el campo de fecha y hora no editable manualmente
        dateTimeEditText.isEnabled = true
        dateTimeEditText.keyListener = null // Deshabilitar entrada de texto

        // Configurar DatePicker y TimePicker
        dateTimeEditText.setOnClickListener { showDateTimePicker() }

        // Configurar RecyclerView para motivos
        reasonsRecyclerView.layoutManager = LinearLayoutManager(context)
        val reasonsAdapter = ReasonsAdapter(
            reasons = emptyList(),
            onEdit = { index, reason ->
                showEditReasonDialog(index, reason)
            },
            onDelete = { index ->
                viewModel.deleteReason(index)
            }
        )
        reasonsRecyclerView.adapter = reasonsAdapter

        // Botón de retroceso
        view.findViewById<ImageView>(R.id.imageView3)?.setOnClickListener {
            viewModel.clearSelections()
            parentFragmentManager.popBackStack()
        }

        // Botón Cancelar
        view.findViewById<TextView>(R.id.button_cancel)?.setOnClickListener {
            clearFields()
            viewModel.clearSelections()
            parentFragmentManager.popBackStack()
        }

        // Botón Guardar
        view.findViewById<TextView>(R.id.button_register_service)?.setOnClickListener {
            val date = dateTimeEditText.text.toString().trim()
            val clientCI = clientSelectText.tag?.toString() ?: ""
            val motorcycleLicensePlate = motorcycleSelectText.tag?.toString() ?: ""
            val reason = reasonInputLayout.editText?.text.toString().trim()
            Log.d(tag, "Register button clicked: date=$date, clientCI=$clientCI, motorcycleLicensePlate=$motorcycleLicensePlate, reason=$reason")
            viewModel.validateAndRegister(date, clientCI, motorcycleLicensePlate, reason)
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
            val clientCI = bundle.getString("client_id") ?: ""
            val clientName = bundle.getString("client_name") ?: "Cliente Seleccionado"
            Log.d(tag, "Received client selection: clientCI=$clientCI, clientName=$clientName")
            viewModel.setClient(clientCI, clientName)
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
            val motorcycleLicensePlate = bundle.getString("motorcycle_details") ?: ""
            val motorcycleDetails = bundle.getString("motorcycle_id") ?: "Motocicleta Seleccionada"
            Log.d(tag, "Received motorcycle selection: licensePlate=$motorcycleLicensePlate, details=$motorcycleDetails")
            viewModel.setMotorcycle(motorcycleLicensePlate, motorcycleDetails)
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
            }
        }
        viewModel.reasonsList.observe(viewLifecycleOwner) { reasons ->
            reasonsAdapter.notifyDataSetChanged()
            reasonsRecyclerView.adapter = ReasonsAdapter(
                reasons = reasons,
                onEdit = { index, reason ->
                    showEditReasonDialog(index, reason)
                },
                onDelete = { index ->
                    viewModel.deleteReason(index)
                }
            )
        }
        viewModel.photosCount.observe(viewLifecycleOwner) { count ->
            photosCountText.text = "Fotos Adjuntadas ($count)"
        }

        // Observar selecciones de cliente y motocicleta
        viewModel.selectedClient.observe(viewLifecycleOwner) { (clientCI, clientName) ->
            if (clientCI != null && clientName != null) {
                clientSelectText.text = clientName
                clientSelectText.tag = clientCI
            } else {
                clientSelectText.text = "SELECCIONAR"
                clientSelectText.tag = null
            }
        }
        viewModel.selectedMotorcycle.observe(viewLifecycleOwner) { (motorcycleLicensePlate, motorcycleDetails) ->
            if (motorcycleLicensePlate != null && motorcycleDetails != null) {
                motorcycleSelectText.text = motorcycleDetails
                motorcycleSelectText.tag = motorcycleLicensePlate
            } else {
                motorcycleSelectText.text = "SELECCIONAR"
                motorcycleSelectText.tag = null
            }
        }

        // Observar fecha seleccionada
        viewModel.selectedDateTime.observe(viewLifecycleOwner) { dateTime ->
            dateTimeEditText.setText(dateTime ?: "")
        }

        return view
    }

    private fun showEditReasonDialog(index: Int, currentReason: String) {
        val editText = EditText(requireContext()).apply {
            setText(currentReason)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Motivo")
            .setView(editText)
            .setPositiveButton("Guardar") { _, _ ->
                val newReason = editText.text.toString().trim()
                if (newReason.isNotEmpty()) {
                    viewModel.editReason(index, newReason)
                } else {
                    Toast.makeText(context, "El motivo no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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
                        val formattedDate = dateFormat.format(calendar.time)
                        dateTimeEditText.setText(formattedDate)
                        viewModel.setDateTime(formattedDate)
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
        viewModel.setDateTime(null)
    }
}