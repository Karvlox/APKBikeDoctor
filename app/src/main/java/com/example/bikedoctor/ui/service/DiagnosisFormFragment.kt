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
import com.example.bikedoctor.data.model.Diagnostic
import com.example.bikedoctor.ui.main.SessionViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class DiagnosisFormFragment : Fragment() {

    private val viewModel: DiagnosisFormViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val tag = "DiagnosisFormFragment"
    private var currentToken: String? = null

    private lateinit var dateTimeInputLayout: TextInputLayout
    private lateinit var dateTimeEditText: TextInputEditText
    private lateinit var clientText: TextView
    private lateinit var motorcycleText: TextView
    private lateinit var errorInputLayout: TextInputLayout
    private lateinit var errorDetailInputLayout: TextInputLayout
    private lateinit var timeSpentInputLayout: TextInputLayout
    private lateinit var photosCountText: TextView
    private lateinit var diagnosticsRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_diagnosis_form layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_diagnosis_form, container, false)
        } catch (e: Exception) {
            Log.e(tag, "Error inflating layout: ${e.message}", e)
            Toast.makeText(context, "Error al inflar el layout: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }

        try {
            dateTimeInputLayout = view.findViewById(R.id.date_time_input_layout)
                ?: throw IllegalStateException("date_time_input_layout no encontrado")
            dateTimeEditText = dateTimeInputLayout.editText as TextInputEditText
            clientText = view.findViewById(R.id.client_text)
                ?: throw IllegalStateException("client_text no encontrado")
            motorcycleText = view.findViewById(R.id.motorcycle_text)
                ?: throw IllegalStateException("motorcycle_text no encontrado")
            errorInputLayout = view.findViewById(R.id.error_input_layout)
                ?: throw IllegalStateException("error_input_layout no encontrado")
            errorDetailInputLayout = view.findViewById(R.id.error_detail_input_layout)
                ?: throw IllegalStateException("error_detail_input_layout no encontrado")
            timeSpentInputLayout = view.findViewById(R.id.time_spent_input_layout)
                ?: throw IllegalStateException("time_spent_input_layout no encontrado")
            photosCountText = view.findViewById(R.id.photos_count_text)
                ?: throw IllegalStateException("photos_count_text no encontrado")
            diagnosticsRecyclerView = view.findViewById(R.id.diagnostics_recycler_view)
                ?: throw IllegalStateException("diagnostics_recycler_view no encontrado")
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
            val diagnosisId = args.getString("diagnosis_id")
            val date = args.getString("diagnosis_date")
            val clientCI = args.getString("diagnosis_clientCI")
            val clientName = args.getString("diagnosis_clientName")
            val motorcycleLicensePlate = args.getString("diagnosis_motorcycleLicensePlate")
            val motorcycleDetails = args.getString("diagnosis_motorcycleDetails")
            val employeeCI = args.getString("diagnosis_employeeCI")
            val diagnostics = args.getParcelableArray("diagnosis_listDiagnostic")?.map { it as Diagnostic }?.toList() ?: emptyList()
            val images = args.getStringArray("diagnosis_images")?.toList()
            val reviewed = args.getBoolean("diagnosis_reviewed", false)

            if (diagnosisId != null) {
                titleTextView.text = "Editar Diagnóstico"
                viewModel.initializeDiagnosis(
                    id = diagnosisId,
                    date = date,
                    clientCI = clientCI,
                    clientName = clientName ?: "Cliente $clientCI",
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    motorcycleDetails = motorcycleDetails ?: motorcycleLicensePlate,
                    employeeCI = employeeCI,
                    diagnostics = diagnostics,
                    images = images,
                    reviewed = reviewed
                )
            }
        }

        dateTimeEditText.isEnabled = true
        dateTimeEditText.keyListener = null

        dateTimeEditText.setOnClickListener { showDateTimePicker() }

        diagnosticsRecyclerView.layoutManager = LinearLayoutManager(context)
        val diagnosticsAdapter = DiagnosticsAdapter(
            diagnosis = emptyList(),
            onEdit = { index, diagnostic ->
                showEditDiagnosticDialog(index, diagnostic)
            },
            onDelete = { index ->
                viewModel.deleteDiagnostic(index)
            }
        )
        diagnosticsRecyclerView.adapter = diagnosticsAdapter

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
            val error = errorInputLayout.editText?.text.toString().trim()
            val errorDetail = errorDetailInputLayout.editText?.text.toString().trim()
            val timeSpent = timeSpentInputLayout.editText?.text.toString().trim()
            Log.d(tag, "Save button clicked: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, error=$error")
            viewModel.validateAndRegister(date, clientCI, motorcycleLicensePlate, error, errorDetail, timeSpent, currentToken)
        }

        view.findViewById<TextView>(R.id.add_diagnostic_button)?.setOnClickListener {
            val error = errorInputLayout.editText?.text.toString().trim()
            val errorDetail = errorDetailInputLayout.editText?.text.toString().trim()
            val timeSpent = timeSpentInputLayout.editText?.text.toString().trim()
            viewModel.addDiagnostic(error, errorDetail, timeSpent)
            errorInputLayout.editText?.text?.clear()
            errorDetailInputLayout.editText?.text?.clear()
            timeSpentInputLayout.editText?.text?.clear()
        }

        view.findViewById<ImageView>(R.id.camera_button)?.setOnClickListener {
            viewModel.addPhoto("photo_uri_${System.currentTimeMillis()}")
        }

        view.findViewById<ImageView>(R.id.gallery_button)?.setOnClickListener {
            viewModel.addPhoto("photo_uri_${System.currentTimeMillis()}")
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
            errorInputLayout.error = error
        }
        viewModel.errorDetailError.observe(viewLifecycleOwner) { error ->
            errorDetailInputLayout.error = error
        }
        viewModel.timeSpentError.observe(viewLifecycleOwner) { error ->
            timeSpentInputLayout.error = error
        }
        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Diagnóstico registrado") || status.startsWith("Diagnóstico actualizado")) {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }
        viewModel.diagnosticsList.observe(viewLifecycleOwner) { diagnostics ->
            diagnosticsAdapter.notifyDataSetChanged()
            diagnosticsRecyclerView.adapter = DiagnosticsAdapter(
                diagnosis = diagnostics,
                onEdit = { index, diagnostic ->
                    showEditDiagnosticDialog(index, diagnostic)
                },
                onDelete = { index ->
                    viewModel.deleteDiagnostic(index)
                }
            )
        }
        viewModel.photosCount.observe(viewLifecycleOwner) { count ->
            photosCountText.text = "Fotos Adjuntadas ($count)"
        }
        viewModel.selectedClient.observe(viewLifecycleOwner) { (clientCI, clientName) ->
            if (clientCI != null && clientName != null) {
                clientText.text = clientName
                clientText.tag = clientCI
            } else {
                clientText.text = "Cliente no seleccionado"
                clientText.tag = null
            }
        }
        viewModel.selectedMotorcycle.observe(viewLifecycleOwner) { (licensePlate, details) ->
            if (licensePlate != null && details != null) {
                motorcycleText.text = details
                motorcycleText.tag = licensePlate
            } else {
                motorcycleText.text = "Motocicleta no seleccionada"
                motorcycleText.tag = null
            }
        }
        viewModel.selectedDateTime.observe(viewLifecycleOwner) { dateTime ->
            dateTimeEditText.setText(dateTime ?: "")
        }

        return view
    }

    private fun showEditDiagnosticDialog(index: Int, currentDiagnostic: Diagnostic) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_diagnostic, null)
        val errorEditText = view.findViewById<EditText>(R.id.edit_error)
        val errorDetailEditText = view.findViewById<EditText>(R.id.edit_error_detail)
        val timeSpentEditText = view.findViewById<EditText>(R.id.edit_time_spent)

        errorEditText.setText(currentDiagnostic.error)
        errorDetailEditText.setText(currentDiagnostic.detailOfError)
        timeSpentEditText.setText(currentDiagnostic.timeSpent?.toString() ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Diagnóstico")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val newError = errorEditText.text.toString().trim()
                val newErrorDetail = errorDetailEditText.text.toString().trim()
                val newTimeSpent = timeSpentEditText.text.toString().trim()
                viewModel.editDiagnostic(index, newError, newErrorDetail, newTimeSpent)
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
        errorInputLayout.editText?.text?.clear()
        errorDetailInputLayout.editText?.text?.clear()
        timeSpentInputLayout.editText?.text?.clear()
        clientText.text = "Cliente no seleccionado"
        clientText.tag = null
        motorcycleText.text = "Motocicleta no seleccionada"
        motorcycleText.tag = null
        viewModel.setDateTime(null)
    }
}