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
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.model.CostApprovalPost
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.SpareParts
import com.example.bikedoctor.data.repository.CostApprovalRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.SparePartsRepository
import com.example.bikedoctor.utils.GetClient
import com.example.bikedoctor.utils.ParserHour
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class SparePartsAdapter(
    context: Context,
    spareParts: List<SpareParts>,
    private val viewModel: SparePartsViewModel,
    private val token: String?
) : ArrayAdapter<SpareParts>(context, 0, spareParts) {

    private val tag = "SparePartsAdapter"
    private val costApprovalRepository = CostApprovalRepository()
    private val sparePartsRepository = SparePartsRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val parseHour = ParserHour()
    private val getClient = GetClient(context)
    private var currentClient: Client? = null

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
        val sparePartsList = spareParts.listSpareParts ?: emptyList()
        val firstSparePart = sparePartsList.firstOrNull()

        idServiceText.text = spareParts.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${spareParts.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${spareParts.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Responsable: ${spareParts.employeeCI ?: "Sin datos"}"
        firstReasonText.text = if (firstSparePart != null) {
            "Lista de Repuestos: ${firstSparePart.nameSparePart}"
        } else {
            "Sin repuestos especificados"
        }

        // Configurar botón de edición
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for spare parts: ${spareParts.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "spareParts_id" to spareParts.id,
                "spareParts_date" to parseHour.parserHourService(spareParts.date.toString()),
                "spareParts_clientCI" to spareParts.clientCI?.toString(),
                "spareParts_motorcycleLicensePlate" to spareParts.motorcycleLicensePlate,
                "spareParts_employeeCI" to spareParts.employeeCI?.toString(),
                "spareParts_listSpareParts" to spareParts.listSpareParts?.toTypedArray(),
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
            createCostApprovalFromSpareParts(spareParts)
        }

        return view
    }

    private fun createCostApprovalFromSpareParts(spareParts: SpareParts) {
        if (spareParts.clientCI == null || spareParts.motorcycleLicensePlate == null || spareParts.employeeCI == null) {
            Log.e(tag, "Cannot create cost approval: Missing required fields")
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
                .setTitle("Continuar con Aprobación de Costos")
                .setMessage("¿Desea notificar al cliente sobre los repuestos?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithCostApproval(spareParts, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithCostApproval(spareParts, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithCostApproval(spareParts: SpareParts, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        val costApproval = CostApprovalPost(
            date = currentDate,
            clientCI = spareParts.clientCI,
            motorcycleLicensePlate = spareParts.motorcycleLicensePlate,
            employeeCI = spareParts.employeeCI,
            listLaborCosts = null
        )

        costApprovalRepository.createCostApprovals(costApproval).enqueue(object : Callback<CostApprovalPost> {
            override fun onResponse(call: Call<CostApprovalPost>, response: Response<CostApprovalPost>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Cost Approval created successfully for spare parts: ${spareParts.id}")
                    spareParts.id?.let { id ->
                        updateSparePartReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Aprobación de costos creada exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (notifyClient) {
                        spareParts.clientCI?.let { ci ->
                            getClient.getClientById(
                                ci = ci,
                                onSuccess = { client ->
                                    currentClient = client
                                    val sparePartsNames = spareParts.listSpareParts?.joinToString(", ") { it.nameSparePart.toString() } ?: "ninguno"
                                    val notification = MessageNotification(
                                        message = "${getGender(client.gender)} ${client.name} ${client.lastName}, los repuestos necesarios para la reparación de su motocicleta son: $sparePartsNames"
                                    )
                                    messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d(tag, "Notification sent successfully for spare parts: ${spareParts.id}")
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
                                },
                                onError = { error ->
                                    Log.e(tag, "No se pudo obtener el cliente: $error")
                                    (context as? FragmentActivity)?.run {
                                        android.widget.Toast.makeText(
                                            this,
                                            "No se pudo obtener la información del cliente para notificar",
                                            android.widget.Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                } else {
                    val errorMsg = "Error al crear aprobación de costos: ${response.code()} ${response.message()}"
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
        sparePartsRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Spare Parts $id marked as reviewed=$reviewed")
                    viewModel.fetchSpareParts(1, 100, token) // Usar el token proporcionado
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Estado actualizado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(tag, "Failed to update spare parts reviewed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado de repuestos: ${response.message()}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(tag, "Error updating spare parts reviewed status: ${t.message}", t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        "Error de conexión al actualizar repuestos: ${t.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun getGender(gender: String): String {
        if (gender == "MASCULINO") {
            return "Estimado"
        } else if (gender == "FEMENINO") {
            return "Estimada"
        }
        return ""
    }
}