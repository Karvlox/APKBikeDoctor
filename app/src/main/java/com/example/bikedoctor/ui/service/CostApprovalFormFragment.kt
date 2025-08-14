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
import com.example.bikedoctor.data.model.LaborCost
import com.example.bikedoctor.ui.main.SessionViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CostApprovalFormFragment : Fragment() {

    private val viewModel: CostApprovalFormViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by activityViewModels()
    private val tag = "CostApprovalFormFragment"
    private var currentToken: String? = null

    private lateinit var dateTimeInputLayout: TextInputLayout
    private lateinit var dateTimeEditText: TextInputEditText
    private lateinit var clientText: TextView
    private lateinit var motorcycleText: TextView
    private lateinit var costApprovalInputLayout: TextInputLayout
    private lateinit var costApprovalEditText: TextInputEditText
    private lateinit var costApprovalDetailInputLayout: TextInputLayout
    private lateinit var costApprovalDetailEditText: TextInputEditText
    private lateinit var priceInputLayout: TextInputLayout
    private lateinit var priceEditText: TextInputEditText
    private lateinit var costApprovalRecyclerView: RecyclerView
    private lateinit var titleTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "Inflating fragment_cost_approval_form layout")
        val view: View
        try {
            view = inflater.inflate(R.layout.fragment_cost_approval_form, container, false)
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
            costApprovalInputLayout = view.findViewById(R.id.costApproval_input_layout)
                ?: throw IllegalStateException("costApproval_input_layout no encontrado")
            costApprovalEditText = view.findViewById(R.id.sparePart_edit_text)
                ?: throw IllegalStateException("sparePart_edit_text no encontrado")
            costApprovalDetailInputLayout = view.findViewById(R.id.costApproval_detail_input_layout)
                ?: throw IllegalStateException("costApproval_detail_input_layout no encontrado")
            costApprovalDetailEditText = view.findViewById(R.id.spare_part_detail_edit_text)
                ?: throw IllegalStateException("spare_part_detail_edit_text no encontrado")
            priceInputLayout = view.findViewById(R.id.price_input_layout)
                ?: throw IllegalStateException("price_input_layout no encontrado")
            priceEditText = view.findViewById(R.id.price_edit_text)
                ?: throw IllegalStateException("price_edit_text no encontrado")
            costApprovalRecyclerView = view.findViewById(R.id.costApproval_recycler_view)
                ?: throw IllegalStateException("costApproval_recycler_view no encontrado")
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
            val costApprovalId = args.getString("costApproval_id")
            val date = args.getString("costApproval_date")
            val clientCI = args.getString("costApproval_clientCI")
            val motorcycleLicensePlate = args.getString("costApproval_motorcycleLicensePlate")
            val employeeCI = args.getString("costApproval_employeeCI")
            val costApprovals = args.getParcelableArray("costApproval_listDiagnostic")?.map { it as LaborCost }?.toList() ?: emptyList()
            val reviewed = args.getBoolean("costApproval_reviewed", false)

            Log.d(tag, "Arguments received - costApprovalId: $costApprovalId, clientCI: $clientCI, motorcycleLicensePlate: $motorcycleLicensePlate")

            if (costApprovalId != null) {
                titleTextView.text = "Editar Aprobación de Costos"
                viewModel.initializeCostApproval(
                    id = costApprovalId,
                    date = date,
                    clientCI = clientCI,
                    motorcycleLicensePlate = motorcycleLicensePlate,
                    employeeCI = employeeCI,
                    costApprovals = costApprovals,
                    reviewed = reviewed
                )
            }
        }

        dateTimeEditText.isEnabled = true
        dateTimeEditText.keyListener = null

        dateTimeEditText.setOnClickListener { showDateTimePicker() }

        costApprovalRecyclerView.layoutManager = LinearLayoutManager(context)
        val costApprovalAdapter = CostApprovalAdapterList(
            costApproval = emptyList(),
            onEdit = { index, costApproval ->
                showEditSparePartDialog(index, costApproval)
            },
            onDelete = { index ->
                viewModel.deleteCostApproval(index)
            }
        )
        costApprovalRecyclerView.adapter = costApprovalAdapter

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
            val costApproval = costApprovalEditText.text.toString().trim()
            val costApprovalDetail = costApprovalDetailEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            Log.d(tag, "Save button clicked: date=$date, clientCI=$clientCI, motorcycle=$motorcycleLicensePlate, costApproval=$costApproval")
            viewModel.validateAndRegister(date, clientCI, motorcycleLicensePlate, costApproval, costApprovalDetail, price, currentToken)
        }

        view.findViewById<TextView>(R.id.add_button)?.setOnClickListener {
            val costApproval = costApprovalEditText.text.toString().trim()
            val costApprovalDetail = costApprovalDetailEditText.text.toString().trim()
            val price = priceEditText.text.toString().trim()
            viewModel.addSparePart(costApproval, costApprovalDetail, price)
            costApprovalEditText.text?.clear()
            costApprovalDetailEditText.text?.clear()
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
            costApprovalInputLayout.error = error
        }
        viewModel.errorDetailError.observe(viewLifecycleOwner) { error ->
            costApprovalDetailInputLayout.error = error
        }
        viewModel.timeSpentError.observe(viewLifecycleOwner) { error ->
            priceInputLayout.error = error
        }
        viewModel.registerStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Aprobación de costos registrada") || status.startsWith("Aprobación de costos actualizada")) {
                clearFields()
                parentFragmentManager.popBackStack()
            }
        }
        viewModel.costApprovalList.observe(viewLifecycleOwner) { costApprovals ->
            costApprovalAdapter.notifyDataSetChanged()
            costApprovalRecyclerView.adapter = CostApprovalAdapterList(
                costApproval = costApprovals,
                onEdit = { index, costApproval ->
                    showEditSparePartDialog(index, costApproval)
                },
                onDelete = { index ->
                    viewModel.deleteCostApproval(index)
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

    private fun showEditSparePartDialog(index: Int, currentSparePart: LaborCost) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_diagnostic, null)
        val costApprovalEditText = view.findViewById<EditText>(R.id.edit_error)
        val costApprovalDetailEditText = view.findViewById<EditText>(R.id.edit_error_detail)
        val priceEditText = view.findViewById<EditText>(R.id.edit_time_spent)

        costApprovalEditText.setText(currentSparePart.nameProduct)
        costApprovalDetailEditText.setText(currentSparePart.descriptionProduct)
        priceEditText.setText(currentSparePart.price)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Aprobación de Costos")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val newSparePart = costApprovalEditText.text.toString().trim()
                val newSparePartDetail = costApprovalDetailEditText.text.toString().trim()
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
        costApprovalEditText.text?.clear()
        costApprovalDetailEditText.text?.clear()
        priceEditText.text?.clear()
        clientText.text = "Cliente no seleccionado"
        clientText.tag = null
        motorcycleText.text = "Motocicleta no seleccionada"
        motorcycleText.tag = null
        viewModel.setDateTime(null)
    }
}