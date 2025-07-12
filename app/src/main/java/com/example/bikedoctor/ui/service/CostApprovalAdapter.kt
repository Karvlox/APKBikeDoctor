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
import com.example.bikedoctor.data.model.CostApproval
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.RepairPost
import com.example.bikedoctor.data.repository.CostApprovalRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.RepairRepository
import com.example.bikedoctor.utils.GetClient
import com.example.bikedoctor.utils.ParserHour
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class CostApprovalAdapter(
    context: Context,
    costApprovals: List<CostApproval>,
    private val viewModel: CostApprovalViewModel,
    private val token: String?
) : ArrayAdapter<CostApproval>(context, 0, costApprovals) {

    private val tag = "CostApprovalAdapter"
    private val repairRepository = RepairRepository()
    private val costApprovalRepository = CostApprovalRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val parseHour = ParserHour()
    private val getClient = GetClient(context)
    private var currentClient: Client? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val costApproval = getItem(position)
        if (costApproval == null) {
            Log.e(tag, "Cost Approval at position $position is null")
            return view
        }

        Log.d(tag, "Rendering cost approval: id=${costApproval.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)
        val costApprovalList = costApproval.listLaborCosts ?: emptyList()
        val firstCostApproval = costApprovalList.firstOrNull()

        idServiceText.text = costApproval.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${costApproval.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${costApproval.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Responsable: ${costApproval.employeeCI ?: "Sin datos"}"
        firstReasonText.text = if (firstCostApproval != null) {
            "Lista de Costos: ${firstCostApproval.nameProduct}"
        } else {
            "Sin costos especificados"
        }

        // Configurar botón de edición
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for cost approval: ${costApproval.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "costApproval_id" to costApproval.id,
                "costApproval_date" to parseHour.parserHourService(costApproval.date.toString()),
                "costApproval_clientCI" to costApproval.clientCI?.toString(),
                "costApproval_motorcycleLicensePlate" to costApproval.motorcycleLicensePlate,
                "costApproval_employeeCI" to costApproval.employeeCI?.toString(),
                "costApproval_listLaborCosts" to costApproval.listLaborCosts?.toTypedArray(),
                "costApproval_reviewed" to costApproval.reviewed
            )
            val costApprovalFormFragment = CostApprovalFormFragment().apply {
                arguments = bundle
            }
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, costApprovalFormFragment)
                .addToBackStack(null)
                .commit()
        }

        // Configurar botón de continuación
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for cost approval: ${costApproval.id}")
            createRepairFromCostApproval(costApproval)
        }

        return view
    }

    private fun createRepairFromCostApproval(costApproval: CostApproval) {
        if (costApproval.clientCI == null || costApproval.motorcycleLicensePlate == null || costApproval.employeeCI == null) {
            Log.e(tag, "Cannot create repair: Missing required fields")
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
                .setTitle("Continuar con Reparación")
                .setMessage("¿Desea notificar al cliente sobre la aprobación de costos?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithRepair(costApproval, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithRepair(costApproval, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithRepair(costApproval: CostApproval, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

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
                    Log.d(tag, "Repair created successfully for cost approval: ${costApproval.id}")
                    costApproval.id?.let { id ->
                        updateCostApprovalReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Reparación creada exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (notifyClient) {
                        costApproval.clientCI?.let { ci ->
                            getClient.getClientById(
                                ci = ci,
                                onSuccess = { client ->
                                    currentClient = client
                                    val laborCostsNames = costApproval.listLaborCosts?.joinToString(", ") { it.nameProduct.toString() } ?: "ninguno"
                                    val notification = MessageNotification(
                                        message = "${getGender(client.gender)} ${client.name} ${client.lastName}, las reparaciones que se realizarán a su motocicleta incluyen los costos: $laborCostsNames"
                                    )
                                    messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d(tag, "Notification sent successfully for cost approval: ${costApproval.id}")
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
                    val errorMsg = "Error al crear reparación: ${response.code()} ${response.message()}"
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

    private fun updateCostApprovalReviewedStatus(id: String, reviewed: Boolean) {
        costApprovalRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Cost Approval $id marked as reviewed=$reviewed")
                    viewModel.fetchCostApprovals(1, 100, token) // Usar el token proporcionado
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Estado actualizado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(tag, "Failed to update cost approval reviewed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado de aprobación de costos: ${response.message()}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(tag, "Error updating cost approval reviewed status: ${t.message}", t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        "Error de conexión al actualizar aprobación de costos: ${t.message}",
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