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
import com.example.bikedoctor.data.model.DiagnosisPost
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.Reception
import com.example.bikedoctor.data.repository.DiagnosisRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.ReceptionRepository
import com.example.bikedoctor.utils.ParserHour
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ReceptionAdapter(context: Context, receptions: List<Reception>) :
    ArrayAdapter<Reception>(context, 0, receptions) {

    private val tag = "ReceptionAdapter"
    private val diagnosisRepository = DiagnosisRepository()
    private val receptionRepository = ReceptionRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val parseHour = ParserHour()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val reception = getItem(position)
        if (reception == null) {
            Log.e(tag, "Reception at position $position is null")
            return view
        }

        Log.d(tag, "Rendering reception: id=${reception.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = reception.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${reception.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${reception.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Responsable: ${reception.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Motivos manifestados: ${reception.reasons?.firstOrNull() ?: "Sin motivos especificados"}"

        // Configurar botón de edición
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${reception.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager

            val bundle = bundleOf(
                "reception_id" to reception.id,
                "reception_date" to parseHour.parserHourService(reception.date.toString()),
                "reception_clientCI" to reception.clientCI?.toString(),
                "reception_motorcycleLicensePlate" to reception.motorcycleLicensePlate,
                "reception_employeeCI" to reception.employeeCI?.toString(),
                "reception_reasons" to reception.reasons?.toTypedArray(),
                "reception_images" to reception.images?.toTypedArray(),
                "reception_reviewed" to reception.reviewed
            )
            val receptionFormFragment = ReceptionFormFragment().apply {
                arguments = bundle
            }
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, receptionFormFragment)
                .addToBackStack(null)
                .commit()
        }

        // Configurar botón de continuación
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${reception.id}")
            createDiagnosisFromReception(reception)
        }

        return view
    }

    private fun createDiagnosisFromReception(reception: Reception) {
        // Validate required fields
        if (reception.clientCI == null || reception.motorcycleLicensePlate == null || reception.employeeCI == null) {
            Log.e(tag, "Cannot create diagnosis: Missing required fields")
            (context as? FragmentActivity)?.run {
                android.widget.Toast.makeText(
                    this,
                    "Error: Faltan datos requeridos (Cliente, Motocicleta o Empleado)",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        // Show dialog to choose notification option
        (context as? FragmentActivity)?.run {
            AlertDialog.Builder(this)
                .setTitle("Continuar con Diagnóstico")
                .setMessage("¿Desea notificar al cliente sobre el diagnóstico?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithDiagnosis(reception, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithDiagnosis(reception, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithDiagnosis(reception: Reception, notifyClient: Boolean) {
        // Get current date in ISO 8601 format
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        // Create DiagnosisPost object
        val diagnosis = DiagnosisPost(
            date = currentDate,
            clientCI = reception.clientCI,
            motorcycleLicensePlate = reception.motorcycleLicensePlate,
            employeeCI = reception.employeeCI,
            listDiagnostic = null
        )

        // Make POST request for diagnosis
        diagnosisRepository.createDiagnosis(diagnosis).enqueue(object : Callback<DiagnosisPost> {
            override fun onResponse(call: Call<DiagnosisPost>, response: Response<DiagnosisPost>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Diagnosis created successfully for reception: ${reception.id}")
                    // Update reception's reviewed status
                    reception.id?.let { id ->
                        updateReceptionReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Diagnóstico creado exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    // If notifyClient is true, send notification
                    if (notifyClient) {
                        val notification = MessageNotification(
                            message = "Se ha creado un diagnóstico para su motocicleta (${reception.motorcycleLicensePlate}) en la fecha $currentDate."
                        )
                        messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d(tag, "Notification sent successfully for reception: ${reception.id}")
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

            override fun onFailure(call: Call<DiagnosisPost>, t: Throwable) {
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

    private fun updateReceptionReviewedStatus(id: String, reviewed: Boolean) {
        receptionRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Reception $id marked as reviewed=$reviewed")
                    (context as? FragmentActivity)?.run {
                        val viewModel = ViewModelProvider(this)
                            .get(ReceptionViewModel::class.java)
                        viewModel.fetchReceptions(1, 100)
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