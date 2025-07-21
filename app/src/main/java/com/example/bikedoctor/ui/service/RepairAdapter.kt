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
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.QualityControlPost
import com.example.bikedoctor.data.model.Repair
import com.example.bikedoctor.data.repository.ControlRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.RepairRepository
import com.example.bikedoctor.utils.GetClient
import com.example.bikedoctor.utils.ParserHour
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class RepairAdapter(
    context: Context,
    repairs: List<Repair>,
    private val viewModel: RepairViewModel,
    private val token: String?
) : ArrayAdapter<Repair>(context, 0, repairs) {

    private val tag = "RepairAdapter"
    private val controlRepository = ControlRepository()
    private val repairRepository = RepairRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val parseHour = ParserHour()
    private val getClient = GetClient(context)
    private var currentClient: Client? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val repair = getItem(position)
        if (repair == null) {
            Log.e(tag, "Repair at position $position is null")
            return view
        }

        Log.d(tag, "Rendering repair: id=${repair.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)
        val reparationList = repair.listReparations ?: emptyList()
        val firstReparation = reparationList.firstOrNull()

        idServiceText.text = repair.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${repair.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${repair.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Responsable: ${repair.employeeCI ?: "Sin datos"}"
        firstReasonText.text = if (firstReparation != null) {
            "Lista de Reparaciones: ${firstReparation.nameReparation}"
        } else {
            "Sin reparaciones especificadas"
        }

        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for repair: ${repair.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "repair_id" to repair.id,
                "repair_date" to parseHour.parserHourService(repair.date.toString()),
                "repair_clientCI" to repair.clientCI?.toString(),
                "repair_motorcycleLicensePlate" to repair.motorcycleLicensePlate,
                "repair_employeeCI" to repair.employeeCI?.toString(),
                "repair_listReparations" to repair.listReparations?.toTypedArray(),
                "repair_reviewed" to repair.reviewed
            )
            val repairFormFragment = RepairFormFragment().apply {
                arguments = bundle
            }
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, repairFormFragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for repair: ${repair.id}")
            createControlFromRepair(repair)
        }

        return view
    }

    private fun createControlFromRepair(repair: Repair) {
        if (repair.clientCI == null || repair.motorcycleLicensePlate == null || repair.employeeCI == null) {
            Log.e(tag, "Cannot create quality control: Missing required fields")
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
                .setTitle("Continuar con Control de Calidad")
                .setMessage("¿Desea notificar al cliente sobre el control de calidad?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithControl(repair, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithControl(repair, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithControl(repair: Repair, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        val control = QualityControlPost(
            date = currentDate,
            clientCI = repair.clientCI,
            motorcycleLicensePlate = repair.motorcycleLicensePlate,
            employeeCI = repair.employeeCI,
            listControls = null
        )

        controlRepository.createControls(control).enqueue(object : Callback<QualityControlPost> {
            override fun onResponse(call: Call<QualityControlPost>, response: Response<QualityControlPost>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Quality Control created successfully for repair: ${repair.id}")
                    repair.id?.let { id ->
                        updateRepairReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Control de Calidad creado exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (notifyClient) {
                        repair.clientCI?.let { ci ->
                            getClient.getClientById(
                                ci = ci,
                                onSuccess = { client ->
                                    currentClient = client
                                    val reparationNames = repair.listReparations?.joinToString(", ") { it.nameReparation.toString() } ?: "ninguno"
                                    val notification = MessageNotification(
                                        message = "${getGender(client.gender)} ${client.name} ${client.lastName}, se han realizado las reparaciones ($reparationNames) a su motocicleta y ahora pasará a la fase de control de calidad para verificar su correcto funcionamiento."

                                    )
                                    messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d(tag, "Notification sent successfully for repair: ${repair.id}")
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
                    val errorMsg = "Error al crear control de calidad: ${response.code()} ${response.message()}"
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

            override fun onFailure(call: Call<QualityControlPost>, t: Throwable) {
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

    private fun updateRepairReviewedStatus(id: String, reviewed: Boolean) {
        repairRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Repair $id marked as reviewed=$reviewed")
                    viewModel.fetchRepairs(1, 100, token) // Usar el token proporcionado
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Estado actualizado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(tag, "Failed to update repair reviewed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado de reparación: ${response.message()}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(tag, "Error updating repair reviewed status: ${t.message}", t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        "Error de conexión al actualizar reparación: ${t.message}",
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