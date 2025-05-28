package com.example.bikedoctor.ui.service

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.RepairPost
import com.example.bikedoctor.data.repository.CostApprovalRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.RepairRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*



class CostApprovalAdapter(context: Context, costApproval: List<CostApproval>) :
    ArrayAdapter<CostApproval>(context, 0, costApproval) {

    private val tag = "CostApprovalAdapter"
    private val repairRepository = RepairRepository()
    private val costApprovalRepository = CostApprovalRepository()
    private val messageNotificationRepository = MessageNotificationRepository()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val costApproval = getItem(position)
        if (costApproval == null) {
            Log.e(tag, "Cost Approval at position $position is null")
            return view
        }

        Log.d(tag, "Rendering Cost Approval: id=${costApproval.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = costApproval.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${costApproval.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${costApproval.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${costApproval.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Lista de Aprobacion de Costos: ${costApproval.listLaborCosts?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${costApproval.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "costApproval_id" to costApproval.id,
                "costApproval_date" to costApproval.date,
                "costApproval_clientCI" to costApproval.clientCI?.toString(),
                "costApproval_motorcycleLicensePlate" to costApproval.motorcycleLicensePlate,
                "costApproval_employeeCI" to costApproval.employeeCI?.toString(),
                "costApproval_listDiagnostic" to costApproval.listLaborCosts?.toTypedArray(),
                "costApproval_reviewed" to costApproval.reviewed
            )
                val sparePartsFormFragment = CostApprovalFormFragment().apply {
                arguments = bundle
            }
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, sparePartsFormFragment)
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${costApproval.id}")
            createRepairFromCostApproval(costApproval)
        }

        return view
    }

    private fun createRepairFromCostApproval(costApproval: CostApproval) {
        if (costApproval.clientCI == null || costApproval.motorcycleLicensePlate == null || costApproval.employeeCI == null) {
            Log.e(tag, "Cannot create costApproval: Missing required fields")
            (context as? FragmentActivity)?.run {
                android.widget.Toast.makeText(
                    this,
                    "Error: Faltan datos requeridos (Cliente, Motocicleta o Empleado)",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        (context as? FragmentActivity)?.run {
            AlertDialog.Builder(this)
                .setTitle("Continuar con Aprobacion de Costos")
                .setMessage("¿Desea notificar al cliente sobre los Repuestos?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithSpareParts(costApproval, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithSpareParts(costApproval, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithSpareParts(costApproval: CostApproval, notifyClient: Boolean) {// Get current date in ISO 8601 format
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        // Create DiagnosisPost object
        val repair = RepairPost(
            date = currentDate,
            clientCI = costApproval.clientCI,
            motorcycleLicensePlate = costApproval.motorcycleLicensePlate,
            employeeCI = costApproval.employeeCI,
            listReparations = null
        )

        repairRepository.createRepairs(repair).enqueue(object : Callback<RepairPost> {
            override fun onResponse(call: Call<RepairPost>, response: Response<RepairPost>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Repair created successfully for reception: ${costApproval.id}")
                    costApproval.id?.let { id ->
                        updateSparePartReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Reparacion creada exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    // If notifyClient is true, send notification
                    if (notifyClient) {
                        val notification = MessageNotification(
                            message = "Se ha creado la reparacion para su motocicleta (${costApproval.motorcycleLicensePlate}) en la fecha $currentDate."
                        )
                        messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d(tag, "Notification sent successfully for reception: ${costApproval.id}")
                                    (context as? FragmentActivity)?.run {
                                        android.widget.Toast.makeText(
                                            this,
                                            "Notificación enviada al cliente",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    val errorMsg = "Error al enviar notificación: ${response.code()} ${response.message()}"
                                    Log.e(tag, errorMsg)
                                    (context as? FragmentActivity)?.run {
                                        android.widget.Toast.makeText(
                                            this,
                                            errorMsg,
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                val errorMsg = "Error de conexión al enviar notificación: ${t.message}"
                                Log.e(tag, errorMsg, t)
                                (context as? FragmentActivity)?.run {
                                    android.widget.Toast.makeText(
                                        this,
                                        errorMsg,
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                    }
                } else {
                    val errorMsg = "Error al crear Reparacion: ${response.code()} ${response.message()}"
                    Log.e(tag, errorMsg)
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            errorMsg,
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<RepairPost>, t: Throwable) {
                val errorMsg = "Error de conexión: ${t.message}"
                Log.e(tag, errorMsg, t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        errorMsg,
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun updateSparePartReviewedStatus(id: String, reviewed: Boolean) {
        costApprovalRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Repair $id marked as reviewed=$reviewed")
                    (context as? FragmentActivity)?.run {
                        val viewModel = ViewModelProvider(this)
                            .get(SparePartsViewModel::class.java)
                        viewModel.fetchReceptions(1, 10)
                    }
                } else {
                    Log.e(tag, "Failed to update reception reviewed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado de recepción: ${response.message()}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(tag, "Error updating reception reviewed status: ${t.message}", t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        "Error de conexión al actualizar: ${t.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
}