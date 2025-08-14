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
import com.example.bikedoctor.data.model.SparePart
import com.example.bikedoctor.ui.main.SessionViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class SparePartsFormFragment : Fragment() {

    private val viewModel: SparePartsFormViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val tag = "SparePartsFormFragment"
    private var currentToken: String? = null

    private lateinit var dateTimeInputLayout: TextInputLayout
    private lateinit var dateTimeEditText: TextInputEditText
    private lateinit var clientText: TextView
    private lateinit var motorcycleText: TextView
    private lateinit var sparePartInputLayout: TextInputLayout
    private lateinit var sparePartEditText: TextInputEditText
    private lateinit var sparePartDetailInputLayout: TextInputLayout
    private lateinit var sparePartDetailEditText: TextInputEditText
    private lateinit var priceInputLayout: TextInputLayout
    private lateinit var priceEditText: TextInputEditText
    private lateinit var sparePartsRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_spare_parts_form layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_spare_parts_form, container, false)
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
            sparePartInputLayout = view.findViewById(R.id.sparePart_input_layout)
                ?: throw IllegalStateException("sparePart_input_layout no encontrado")
            sparePartEditText = view.findViewById(R.id.sparePart_edit_text)
                ?: throw IllegalStateException("sparePart_edit_text no encontrado")
            sparePartDetailInputLayout = view.findViewById(R.id.spare_part_detail_input_layout)
                ?: throw IllegalStateException("spare_part_detail_input_layout no encontrado")
            sparePartDetailEditText = view.findViewById(R.id.spare_part_detail_edit_text)
                ?: throw IllegalStateException("spare_part_detail_edit_text no encontrado")
            priceInputLayout = view.findViewById(R.id.time_spent_input_layout)
                ?: throw IllegalStateException("time_spent_input_layout no encontrado")
            priceEditText = view.findViewById(R.id.price_edit_text)
                ?: throw IllegalStateException("price_edit_text no encontrado")
            sparePartsRecyclerView = view.findViewById(R.id.spare_part_recycler_view)
                ?: throw IllegalStateException("spare_part_recycler_view no encontrado")
            titleTextView = view.findViewById(R.id.title_text)
                ?: throw IllegalStateException("title_text no encontrado")
        } catch (e: Exception) {
            Log.e(tag, "Error initializing views: ${e.message}", e)
            Toast.makeText(context, "Error al inicializar vistas: ${e.message}", Toast.LENGTH_LONG).show()
            return view
        }

        // Observar el token desde SessionViewModel
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
            val sparePartId = args.getString("spareParts_id")
            val date = args.getString("spareParts_date")
            val clientCI = args.getString("spareParts_clientCI")
            val motorcycleLicensePlate = args.getString("spareParts_motorcycleLicensePlate")
            val employeeCI = args.getString("spareParts_employeeCI")
            val spareParts = args.getParcelableArray("spareParts_listDiagnostic")?.map { it as SparePart }?.toList() ?: emptyList()
            val reviewed = args.getBoolean("spareParts_reviewed", false)

            Log.d(tag, "Arguments received - sparePartId: $sparePartId, clientCI: $clientCI, motorcycleLicensePlate: $motorcycleLicensePlate")

            if (sparePartId != null) {
                titleTextView.text = "Editar Repuesto"
                viewModel.initializeSpareParts(
                    id = sparePartId,
                    date = date,
                    clientCI = clientCI,
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = employeeCI,
                    spareParts = spareParts,
                    reviewed = reviewed
                )
            }
        }

        dateTimeEditText.isEnabled = true
        dateTimeEditText.keyListener = null

        dateTimeEditText.setOnClickListener { showDateTimePicker() }

        sparePartsRecyclerView.layoutManager = LinearLayoutManager(context)
        val sparePartsAdapter = SparePartsAdapterList(
            spareParts = emptyList(),
            onEdit = { index, sparePart ->
                showEditSparePartDialog(index, sparePart)
            },
            onDelete = { index ->
                viewModel.deleteSparePart(index)
            }
        )
        sparePartsRecyclerView.adapter = sparePartsAdapter

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
            val sparePart = sparePartEditText.text.toString().trim()
            val sparePartDetail = sparePartDetailEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            Log.d(tag, "Save button clicked: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, sparePart=$sparePart")
            viewModel.validateAndRegister(date, clientCI, motorcycleLicensePlate, sparePart, sparePartDetail, price, currentToken)
        }

        view.findViewById<TextView>(R.id.add_diagnostic_button)?.setOnClickListener {
            val sparePart = sparePartEditText.text.toString().trim()
            val sparePartDetail = sparePartDetailEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            viewModel.addSparePart(sparePart, sparePartDetail, price)
            sparePartEditText.text?.clear()
            sparePartDetailEditText.text?.clear()
            priceEditText.text?.clear()
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
            sparePartInputLayout.error = error
        }
        viewModel.errorDetailError.observe(viewLifecycleOwner) { error ->
            sparePartDetailInputLayout.error = error
        }
        viewModel.timeSpentError.observe(viewLifecycleOwner) { error ->
            priceInputLayout.error = error
        }
        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Repuesto registrado") || status.startsWith("Repuesto actualizado")) {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }
        viewModel.sparePartsList.observe(viewLifecycleOwner) { spareParts ->
            sparePartsAdapter.notifyDataSetChanged()
            sparePartsRecyclerView.adapter = SparePartsAdapterList(
                spareParts = spareParts,
                onEdit = { index, sparePart ->
                    showEditSparePartDialog(index, sparePart)
                },
                onDelete = { index ->
                    viewModel.deleteSparePart(index)
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
                Log.d(tag, "clientText set to: ${clientText.text}")
            } else {
                clientText.text = "Cliente no seleccionado"
                clientText.tag = null
                Log.d(tag, "clientText set to: Cliente no seleccionado (clientCI is null)")
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

    private fun showEditSparePartDialog(index: Int, currentSparePart: SparePart) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_diagnostic, null)
        val sparePartEditText = view.findViewById<EditText>(R.id.edit_error)
        val sparePartDetailEditText = view.findViewById<EditText>(R.id.edit_error_detail)
        val priceEditText = view.findViewById<EditText>(R.id.edit_time_spent)

        sparePartEditText.setText(currentSparePart.nameSparePart)
        sparePartDetailEditText.setText(currentSparePart.detailSparePart)
        priceEditText.setText(currentSparePart.price?.toString() ?: "")

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Repuesto")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val newSparePart = sparePartEditText.text.toString().trim()
                val newSparePartDetail = sparePartDetailEditText.text.toString().trim()
                val newPrice = priceEditText.text.toString().trim()
                viewModel.editSparePart(index, newSparePart, newSparePartDetail, newPrice)
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
        sparePartEditText.text?.clear()
        sparePartDetailEditText.text?.clear()
        priceEditText.text?.clear()
        clientText.text = "Cliente no seleccionado"
        clientText.tag = null
        motorcycleText.text = "Motocicleta no seleccionada"
        motorcycleText.tag = null
        viewModel.setDateTime(null)
    }
}