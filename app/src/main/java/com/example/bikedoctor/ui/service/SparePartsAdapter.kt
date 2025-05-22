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
import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.SpareParts
import com.example.bikedoctor.data.repository.CostApprovalRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.SparePartsRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SparePartsAdapter(context: Context, spareParts: List<SpareParts>) :
    ArrayAdapter<SpareParts>(context, 0, spareParts) {

    private val tag = "SparePartsAdapter"
    private val costApprovalRepository = CostApprovalRepository()
    private val diagnosisRepository = SparePartsRepository()
    private val messageNotificationRepository = MessageNotificationRepository()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val spareParts = getItem(position)
        if (spareParts == null) {
            Log.e(tag, "SpareParts at position $position is null")
            return view
        }

        Log.d(tag, "Rendering spare parts: id=${spareParts.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = spareParts.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${spareParts.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${spareParts.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${spareParts.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Lista de Repuestos: ${spareParts.listSpareParts?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${spareParts.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "spareParts_id" to spareParts.id,
                "spareParts_date" to spareParts.date,
                "spareParts_clientCI" to spareParts.clientCI?.toString(),
                "spareParts_motorcycleLicensePlate" to spareParts.motorcycleLicensePlate,
                "spareParts_employeeCI" to spareParts.employeeCI?.toString(),
                "spareParts_listDiagnostic" to spareParts.listSpareParts?.toTypedArray(),
                "spareParts_images" to spareParts.listSpareParts?.map { it.detailSparePart ?: "" }?.toTypedArray(),
                "spareParts_reviewed" to spareParts.reviewed
            )
            val sparePartsFormFragment = SparePartsFormFragment().apply {
                arguments = bundle
            }
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, sparePartsFormFragment)
                .addToBackStack(null)
                .commit()
        }
        // Configurar botón de continuación
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for spare parts: ${spareParts.id}")
            createSparePartsFromDiagnosis(spareParts)
        }

        return view
    }

    private fun createSparePartsFromDiagnosis(spareParts: SpareParts) {
        if (spareParts.clientCI == null || spareParts.motorcycleLicensePlate == null || spareParts.employeeCI == null) {
            Log.e(tag, "Cannot create spareParts: Missing required fields")
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
                    proceedWithSpareParts(spareParts, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithSpareParts(spareParts, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithSpareParts(sparePart: SpareParts, notifyClient: Boolean) {
        // Get current date in ISO 8601 format
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        // Create DiagnosisPost object
        val costApproval = CostApprovalPost(
            date = currentDate,
            clientCI = sparePart.clientCI,
            motorcycleLicensePlate = sparePart.motorcycleLicensePlate,
            employeeCI = sparePart.employeeCI,
            listLaborCosts = null
        )

        costApprovalRepository.createCostApprovals(costApproval).enqueue(object : Callback<CostApprovalPost> {
            override fun onResponse(call: Call<CostApprovalPost>, response: Response<CostApprovalPost>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Spare Parts created successfully for reception: ${sparePart.id}")
                    sparePart.id?.let { id ->
                        updateSparePartReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Repuestos creado exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    // If notifyClient is true, send notification
                    if (notifyClient) {
                        val notification = MessageNotification(
                            message = "Se ha creado los Repuestos para su motocicleta (${sparePart.motorcycleLicensePlate}) en la fecha $currentDate."
                        )
                        messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d(tag, "Notification sent successfully for reception: ${sparePart.id}")
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
                    val errorMsg = "Error al crear diagnóstico: ${response.code()} ${response.message()}"
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

            override fun onFailure(call: Call<CostApprovalPost>, t: Throwable) {
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
        diagnosisRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Spare Part $id marked as reviewed=$reviewed")
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
                        "Error de conexión al actualizar recepción: ${t.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
}