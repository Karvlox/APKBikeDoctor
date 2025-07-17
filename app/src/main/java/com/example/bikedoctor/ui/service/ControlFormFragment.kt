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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Control
import com.example.bikedoctor.ui.main.SessionViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class ControlFormFragment : Fragment() {

    private val viewModel: ControlFormViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val tag = "ControlFormFragment"
    private var currentToken: String? = null

    private lateinit var dateTimeInputLayout: TextInputLayout
    private lateinit var dateTimeEditText: TextInputEditText
    private lateinit var clientText: TextView
    private lateinit var motorcycleText: TextView
    private lateinit var controlInputLayout: TextInputLayout
    private lateinit var controlEditText: TextInputEditText
    private lateinit var controlDetailInputLayout: TextInputLayout
    private lateinit var controlDetailEditText: TextInputEditText
    private lateinit var controlRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_control_form layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_control_form, container, false)
        } catch (e: Exception) {
            Log.e(tag, "Error inflating layout: ${e.message}", e)
            Toast.makeText(context, "Error al inflar el layout: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }

        try {
            dateTimeInputLayout = view.findViewById(R.id.date_time_input_layout)
                ?: throw IllegalStateException("date_time_input_layout no encontrado")
            dateTimeEditText = view.findViewById(R.id.date_time_edit_text)
                ?: throw IllegalStateException("date_time_edit_text no encontrado")
            clientText = view.findViewById(R.id.client_ci_text)
                ?: throw IllegalStateException("client_text no encontrado")
            motorcycleText = view.findViewById(R.id.motorcycle_text)
                ?: throw IllegalStateException("motorcycle_text no encontrado")
            controlInputLayout = view.findViewById(R.id.control_input_layout)
                ?: throw IllegalStateException("control_input_layout no encontrado")
            controlEditText = view.findViewById(R.id.control_title)
                ?: throw IllegalStateException("control_edit_text no encontrado")
            controlDetailInputLayout = view.findViewById(R.id.control_detail_input_layout)
                ?: throw IllegalStateException("control_detail_input_layout no encontrado")
            controlDetailEditText = view.findViewById(R.id.control_detail_edit_text)
                ?: throw IllegalStateException("control_detail_edit_text no encontrado")
            controlRecyclerView = view.findViewById(R.id.control_recycler_view)
                ?: throw IllegalStateException("control_recycler_view no encontrado")
            titleTextView = view.findViewById(R.id.title_text)
                ?: throw IllegalStateException("title_text no encontrado")
        } catch (e: Exception) {
            Log.e(tag, "Error initializing views: ${e.message}", e)
            Toast.makeText(context, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            return view
        }

        sessionViewModel.token.observe(viewLifecycleOwner) { token ->
            Log.d(tag, "Token observed: $token")
            currentToken = token
            viewModel.setToken(token)
            if (token == null) {
                Log.e(tag, "No token, cannot proceed")
                Toast.makeText(requireContext(), "Sesión no iniciada", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack()
            }
        }

        arguments?.let { args ->
            val controlId = args.getString("control_id")
            val date = args.getString("control_date")
            val clientCI = args.getString("control_clientCI")
            val motorcycleLicensePlate = args.getString("control_motorcycleLicensePlate")
            val employeeCI = args.getString("control_employeeCI")
            val controls = args.getParcelableArray("control_listDiagnostic")?.map { it as Control }?.toList() ?: emptyList()
            val reviewed = args.getBoolean("control_reviewed", false)

            Log.d(tag, "Arguments received - controlId: $controlId, clientCI: $clientCI, motorcycleLicensePlate: $motorcycleLicensePlate")

            if (controlId != null) {
                titleTextView.text = "Editar Controles"
                viewModel.initializeControl(
                    id = controlId,
                    date = date,
                    clientCI = clientCI,
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = employeeCI,
                    listControls = controls,
                    reviewed = reviewed
                )
            }
        }

        dateTimeEditText.isEnabled = true
        dateTimeEditText.keyListener = null

        dateTimeEditText.setOnClickListener { showDateTimePicker() }

        controlRecyclerView.layoutManager = LinearLayoutManager(context)
        val controlAdapter = ControlAdapterList(
            control = emptyList(),
            onEdit = { index, control ->
                showEditControlDialog(index, control)
            },
            onDelete = { index ->
                viewModel.deleteControl(index)
            }
        )
        controlRecyclerView.adapter = controlAdapter

        view.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            viewModel.clearSelections()
            parentFragmentManager.popBackStack()
        }

        view.findViewById<TextView>(R.id.cancel_button)?.setOnClickListener {
            clearFields()
            viewModel.clearSelections()
            parentFragmentManager.popBackStack()
        }

        view.findViewById<TextView>(R.id.save_button)?.setOnClickListener {
            val date = dateTimeEditText.text.toString().trim()
            val clientCI = clientText.tag?.toString() ?: ""
            val motorcycleLicensePlate = motorcycleText.tag?.toString() ?: ""
            val controlName = controlEditText.text.toString().trim()
            val controlDetail = controlDetailEditText.text.toString().trim()
            Log.d(tag, "Save button clicked: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, control=$controlName")
            viewModel.validateAndRegister(date, clientCI, motorcycleLicensePlate, controlName, controlDetail, currentToken)
        }

        view.findViewById<TextView>(R.id.add_button)?.setOnClickListener {
            val controlName = controlEditText.text.toString().trim()
            val controlDetail = controlDetailEditText.text.toString().trim()
            viewModel.addControl(controlName, controlDetail)
            controlEditText.text?.clear()
            controlDetailEditText.text?.clear()
        }

        viewModel.dateTimeError.observe(viewLifecycleOwner) { error ->
            dateTimeInputLayout.error = error
        }
        viewModel.clientError.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
        viewModel.motorcycleError.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
        viewModel.errorDiagnosticError.observe(viewLifecycleOwner) { error ->
            controlInputLayout.error = error
        }
        viewModel.errorDetailError.observe(viewLifecycleOwner) { error ->
            controlDetailInputLayout.error = error
        }
        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Control registrado") || status.startsWith("Control actualizado")) {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }
        viewModel.controlsList.observe(viewLifecycleOwner) { controls ->
            controlAdapter.notifyDataSetChanged()
            controlRecyclerView.adapter = ControlAdapterList(
                control = controls,
                onEdit = { index, control ->
                    showEditControlDialog(index, control)
                },
                onDelete = { index ->
                    viewModel.deleteControl(index)
                }
            )
        }
        viewModel.selectedDateTime.observe(viewLifecycleOwner) { dateTime ->
            dateTimeEditText.setText(dateTime ?: "")
        }

        viewModel.selectedClient.observe(viewLifecycleOwner) { clientCI ->
            Log.d(tag, "Observing selectedClient: $clientCI")
            if (clientCI != null) {
                clientText.text = clientCI
                clientText.tag = clientCI
            } else {
                clientText.text = "Cliente no seleccionado"
                clientText.tag = null
            }
        }
        viewModel.selectedMotorcycle.observe(viewLifecycleOwner) { motorcycleLicensePlate ->
            Log.d(tag, "Observing selectedMotorcycle: $motorcycleLicensePlate")
            if (motorcycleLicensePlate != null) {
                motorcycleText.text = motorcycleLicensePlate
                motorcycleText.tag = motorcycleLicensePlate
                Log.d(tag, "motorcycleText set to: ${motorcycleText.text}")
            } else {
                motorcycleText.text = "Motocicleta no seleccionada"
                motorcycleText.tag = null
            }
        }
        return view
    }

    private fun showEditControlDialog(index: Int, currentControl: Control) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_diagnostic, null)
        val controlEditText = view.findViewById<EditText>(R.id.edit_error)
        val controlDetailEditText = view.findViewById<EditText>(R.id.edit_error_detail)

        controlEditText.setText(currentControl.controlName)
        controlDetailEditText.setText(currentControl.detailsControl)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Control")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = controlEditText.text.toString().trim()
                val newDetail = controlDetailEditText.text.toString().trim()
                viewModel.editControl(index, newName, newDetail)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, selectedHour, selectedMinute ->
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.US)
                        val formattedDate = dateFormat.format(calendar.time)
                        dateTimeEditText.setText(formattedDate)
                        viewModel.setDateTime(formattedDate)
                    },
                    hour,
                    minute,
                    false
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
        controlEditText.text?.clear()
        controlDetailEditText.text?.clear()
        clientText.text = "Cliente no seleccionado"
        clientText.tag = null
        motorcycleText.text = "Motocicleta no seleccionada"
        motorcycleText.tag = null
        viewModel.setDateTime(null)
    }
}