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
import com.example.bikedoctor.data.model.DeliveryPost
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.QualityControl
import com.example.bikedoctor.data.repository.ControlRepository
import com.example.bikedoctor.data.repository.DeliveryRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.utils.GetClient
import com.example.bikedoctor.utils.ParserHour
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ControlAdapter(
    context: Context,
    control: List<QualityControl>,
    private val controlViewModel: ControlViewModel,
    private val token: String?
) : ArrayAdapter<QualityControl>(context, 0, control) {

    private val tag = "ControlAdapter"
    private val deliveryRepository = DeliveryRepository()
    private val costApprovalRepository = ControlRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val parseHour = ParserHour()
    private val getClient = GetClient(context)
    private var currentClient: Client? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val control = getItem(position)
        if (control == null) {
            Log.e(tag, "Control at position $position is null")
            return view
        }

        Log.d(tag, "Rendering control: id=${control.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)
        val sparePartsList = control.listControls ?: emptyList()
        val firstSparePart = sparePartsList.firstOrNull()

        idServiceText.text = control.id ?: "Sin ID"
        nameCIText.text = "Cliente: Cargando..."
        motorcycleClientText.text = "Motocicleta: ${control.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${control.employeeCI ?: "Sin datos"}"
        firstReasonText.text = if (firstSparePart != null) {
            "Lista de Controles: ${firstSparePart.controlName}"
        } else {
            "Sin controles especificados"
        }

        // Obtener el nombre del cliente usando GetClient
        control.clientCI?.let { ci ->
            getClient.getClientById(
                ci = ci,
                onSuccess = { client ->
                    nameCIText.text = "Cliente: ${client.name} ${client.lastName}"
                    Log.d(tag, "Client name fetched: ${client.name} ${client.lastName}")
                },
                onError = { error ->

                    nameCIText.text = "Cliente: ${control.clientCI}"
                    Log.e(tag, "Error fetching client name: $error")
                }
            )
        } ?: run {
            nameCIText.text = "Cliente: Desconocido"
        }

        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${control.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "control_id" to control.id,
                "control_date" to parseHour.parserHourService(control.date.toString()),
                "control_clientCI" to control.clientCI?.toString(),
                "control_motorcycleLicensePlate" to control.motorcycleLicensePlate,
                "control_employeeCI" to control.employeeCI?.toString(),
                "control_listDiagnostic" to control.listControls?.toTypedArray(),
                "control_reviewed" to control.reviewed
            )
            val controlFormFragment = ControlFormFragment().apply {
                arguments = bundle
            }
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, controlFormFragment)
                .addToBackStack(null)
                .commit()
        }
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${control.id}")
            createDeliveryFromControl(control)
        }
        return view
    }

    private fun createDeliveryFromControl(control: QualityControl) {
        if (control.clientCI == null || control.motorcycleLicensePlate == null || control.employeeCI == null) {
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
                .setTitle("Continuar con Control")
                .setMessage("¿Notificar la Reparación?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithReparation(control, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithReparation(control, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithReparation(control: QualityControl, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        val delivery = DeliveryPost(
            date = currentDate,
            clientCI = control.clientCI,
            motorcycleLicensePlate = control.motorcycleLicensePlate,
            employeeCI = control.employeeCI
        )

        deliveryRepository.createDelivery(delivery).enqueue(object : Callback<DeliveryPost> {
            override fun onResponse(call: Call<DeliveryPost>, response: Response<DeliveryPost>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Control created successfully for reception: ${control.id}")
                    control.id?.let { id ->
                        updateReparationReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Control de Calidad creado exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (notifyClient) {
                        control.clientCI?.let { ci ->
                            getClient.getClientById(
                                ci = ci,
                                onSuccess = { client ->
                                    currentClient = client
                                    val notification = MessageNotification(
                                        message = "${getGender(client.gender)} ${client.name + " " + client.lastName} los controles fueron exitosos, su motocicleta esta lista para ser entrega a su persona, puede pasar por el taller mecanico por la Motocicleta."
                                    )
                                    messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d(tag, "Notification sent successfully for reception: ${control.id}")
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

            override fun onFailure(call: Call<DeliveryPost>, t: Throwable) {
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

    private fun updateReparationReviewedStatus(id: String, reviewed: Boolean) {
        costApprovalRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Control $id marked as reviewed=$reviewed")
                    controlViewModel.fetchCards(1, 100, token)
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Estado actualizado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(tag, "Failed to update reception reviewed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado: ${response.message()}",
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

    private fun getGender(gender: String): String {
        if (gender == "MASCULINO") {
            return "Estimado"
        } else if (gender == "FEMENINO") {
            return "Estimada"
        }
        return ""
    }
}