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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Reparation
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.getValue

class RepairFormFragment : Fragment() {

    private val viewModel: RepairFormViewModel by viewModels()
    private val tag = "RepairFormFragment"

    private lateinit var dateTimeInputLayout: TextInputLayout
    private lateinit var dateTimeEditText: TextInputEditText
    private lateinit var clientText: TextView
    private lateinit var motorcycleText: TextView
    private lateinit var repairInputLayout: TextInputLayout
    private lateinit var repairEditText: TextInputEditText
    private lateinit var repairDetailInputLayout: TextInputLayout
    private lateinit var repairDetailEditText: TextInputEditText
    private lateinit var repairRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_repair_form layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_repair_form, container, false)
        } catch (e: Exception) {
            Log.e(tag, "Error inflating layout: ${e.message}", e)
            Toast.makeText(context, "Error al inflar el layout: ${e.message}", Toast.LENGTH_LONG).show()
            return null
        }

        // Initialize views
        try {
            dateTimeInputLayout = view.findViewById(R.id.date_time_input_layout)
                ?: throw IllegalStateException("date_time_input_layout no encontrado")
            dateTimeEditText = view.findViewById(R.id.date_time_edit_text)
                ?: throw IllegalStateException("date_time_edit_text no encontrado")
            clientText = view.findViewById(R.id.client_ci_text)
                ?: throw IllegalStateException("client_text no encontrado")
            motorcycleText = view.findViewById(R.id.motorcycle_text)
                ?: throw IllegalStateException("motorcycle_text no encontrado")
            repairInputLayout = view.findViewById(R.id.repair_input_layout)
                ?: throw IllegalStateException("repair_input_layout no encontrado")
            repairEditText = view.findViewById(R.id.repair_title)
                ?: throw IllegalStateException("repair_edit_text no encontrado")
            repairDetailInputLayout = view.findViewById(R.id.repair_detail_input_layout)
                ?: throw IllegalStateException("repair_detail_input_layout no encontrado")
            repairDetailEditText = view.findViewById(R.id.repair_detail_edit_text)
                ?: throw IllegalStateException("repair_detail_edit_text no encontrado")
            repairRecyclerView = view.findViewById(R.id.repair_recycler_view)
                ?: throw IllegalStateException("repair_recycler_view no encontrado")
            titleTextView = view.findViewById(R.id.title_text)
                ?: throw IllegalStateException("title_text no encontrado")
        } catch (e: Exception) {
            Log.e(tag, "Error initializing views: ${e.message}", e)
            Toast.makeText(context, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            return view
        }

        // Verificar argumentos para modo edición
        arguments?.let { args ->
            val repair = args.getString("repair_id")
            val date = args.getString("repair_date")
            val clientCI = args.getString("repair_clientCI")
            val motorcycleLicensePlate = args.getString("repair_motorcycleLicensePlate")
            val employeeCI = args.getString("repair_employeeCI")
            val repairs = args.getParcelableArray("repair_listDiagnostic")?.map { it as Reparation } ?.toList() ?: emptyList()
            val reviewed = args.getBoolean("repair_reviewed", false)

            Log.d(tag, "Arguments received - repairId: $repair, clientCI: $clientCI, motorcycleLicensePlate: $motorcycleLicensePlate")

            if (repair != null) {
                titleTextView.text = "Editar Reparaciones"
                viewModel.initializeRepair(
                    id = repair,
                    date = date,
                    clientCI = clientCI,
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = employeeCI,
                    reparations = repairs,
                    reviewed = reviewed
                )
            }
        }

        // Hacer el campo de fecha y hora no editable manualmente
        dateTimeEditText.isEnabled = true
        dateTimeEditText.keyListener = null

        // Configurar DatePicker y TimePicker
        dateTimeEditText.setOnClickListener { showDateTimePicker() }

        // Configurar RecyclerView
        repairRecyclerView.layoutManager = LinearLayoutManager(context)
        val repairAdapter = RepairAdapterList(
            control = emptyList(),
            onEdit = { index, control ->
                showEditSparePartDialog(index, control)
            },
            onDelete = { index ->
                viewModel.deleteRepair(index)
            }
        )
        repairRecyclerView.adapter = repairAdapter

        // Botón de retroceso
        view.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            viewModel.clearSelections()
            parentFragmentManager.popBackStack()
        }

        // Botón Cancelar
        view.findViewById<TextView>(R.id.cancel_button)?.setOnClickListener {
            clearFields()
            viewModel.clearSelections()
            parentFragmentManager.popBackStack()
        }

        // Botón Guardar
        view.findViewById<TextView>(R.id.save_button)?.setOnClickListener {
            val date = dateTimeEditText.text.toString().trim()
            val clientCI = clientText.tag?.toString() ?: ""
            val motorcycleLicensePlate = motorcycleText.tag?.toString() ?: ""
            val repairTitle = repairEditText.text.toString().trim()
            val repairDetail = repairDetailEditText.text.toString().trim()
            Log.d(tag, "Save button clicked: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, repair=$repairTitle")
            viewModel.validateAndRegister(date, clientCI, motorcycleLicensePlate, repairTitle, repairDetail)
        }

        // Botón Agregar
        view.findViewById<TextView>(R.id.add_button)?.setOnClickListener {
            val repairTitle = repairEditText.text.toString().trim()
            val repairDetail = repairDetailEditText.text.toString().trim()
            viewModel.addSparePart(repairTitle, repairDetail)
            repairEditText.text?.clear()
            repairDetailEditText.text?.clear()
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
        viewModel.errorDiagnosticError.observe(viewLifecycleOwner) { error ->
            repairInputLayout.error = error
        }
        viewModel.errorDetailError.observe(viewLifecycleOwner) { error ->
            repairDetailInputLayout.error = error
        }
        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Reparación registrada") || status.startsWith("Reparación actualizada")) {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }
        viewModel.repairsList.observe(viewLifecycleOwner) { repairs ->
            repairAdapter.notifyDataSetChanged()
            repairRecyclerView.adapter = RepairAdapterList(
                control = repairs,
                onEdit = { index, control ->
                    showEditSparePartDialog(index, control)
                },
                onDelete = { index ->
                    viewModel.deleteRepair(index)
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

    private fun showEditSparePartDialog(index: Int, currentRepair: Reparation) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_diagnostic, null)
        val repairEditText = view.findViewById<EditText>(R.id.edit_error)
        val repairDetailEditText = view.findViewById<EditText>(R.id.edit_error_detail)

        repairEditText.setText(currentRepair.nameReparation)
        repairDetailEditText.setText(currentRepair.descriptionReparation)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Reparación")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val newSparePart = repairEditText.text.toString().trim()
                val newSparePartDetail = repairDetailEditText.text.toString().trim()
                viewModel.editSparePart(index, newSparePart, newSparePartDetail)
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
        repairEditText.text?.clear()
        repairDetailEditText.text?.clear()
        clientText.text = "Cliente no seleccionado"
        clientText.tag = null
        motorcycleText.text = "Motocicleta no seleccionada"
        motorcycleText.tag = null
        viewModel.setDateTime(null)
    }
}