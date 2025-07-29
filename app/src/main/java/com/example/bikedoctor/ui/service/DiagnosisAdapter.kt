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
import com.example.bikedoctor.data.model.Diagnosis
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.SparePartsPost
import com.example.bikedoctor.data.repository.DiagnosisRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.SparePartsRepository
import com.example.bikedoctor.utils.GetClient
import com.example.bikedoctor.utils.ParserHour
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DiagnosisAdapter(
    context: Context,
    diagnosis: List<Diagnosis>,
    private val viewModel: DiagnosisViewModel,
    private val token: String?
) : ArrayAdapter<Diagnosis>(context, 0, diagnosis) {

    private val tag = "DiagnosisAdapter"
    private val sparePartsRepository = SparePartsRepository()
    private val diagnosisRepository = DiagnosisRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val parseHour = ParserHour()
    private val getClient = GetClient(context)
    private var currentClient: Client? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val diagnosis = getItem(position)
        if (diagnosis == null) {
            Log.e(tag, "Diagnosis at position $position is null")
            return view
        }

        Log.d(tag, "Rendering diagnosis: id=${diagnosis.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)
        val diagnosisList = diagnosis.listDiagnostics ?: emptyList()
        val firstDiagnosis = diagnosisList.firstOrNull()

        idServiceText.text = diagnosis.id ?: "Sin ID"
        nameCIText.text = "Cliente: Cargando..."
        motorcycleClientText.text = "Motocicleta: ${diagnosis.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Responsable: ${diagnosis.employeeCI ?: "Sin datos"}"
        firstReasonText.text = if (firstDiagnosis != null) {
            "Lista de Diagnósticos: ${firstDiagnosis.error}"
        } else {
            "Sin diagnósticos especificados"
        }

        // Obtener el nombre del cliente usando GetClient
        diagnosis.clientCI?.let { ci ->
            getClient.getClientById(
                ci = ci,
                onSuccess = { client ->
                    nameCIText.text = "Cliente: ${client.name} ${client.lastName}"
                    Log.d(tag, "Client name fetched: ${client.name} ${client.lastName}")
                },
                onError = { error ->

                    nameCIText.text = "Cliente: ${diagnosis.clientCI}"
                    Log.e(tag, "Error fetching client name: $error")
                }
            )
        } ?: run {
            nameCIText.text = "Cliente: Desconocido"
        }

        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for diagnosis: ${diagnosis.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "diagnosis_id" to diagnosis.id,
                "diagnosis_date" to parseHour.parserHourService(diagnosis.date.toString()),
                "diagnosis_clientCI" to diagnosis.clientCI?.toString(),
                "diagnosis_motorcycleLicensePlate" to diagnosis.motorcycleLicensePlate,
                "diagnosis_employeeCI" to diagnosis.employeeCI?.toString(),
                "diagnosis_listDiagnostic" to diagnosis.listDiagnostics?.toTypedArray(),
                "diagnosis_images" to diagnosis.listDiagnostics?.map { it.detailOfError ?: "" }?.toTypedArray(),
                "diagnosis_reviewed" to diagnosis.reviewed
            )
            val diagnosisFormFragment = DiagnosisFormFragment().apply {
                arguments = bundle
            }
            fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, diagnosisFormFragment)
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for diagnosis: ${diagnosis.id}")
            createSparePartsFromDiagnosis(diagnosis)
        }

        return view
    }

    private fun createSparePartsFromDiagnosis(diagnosis: Diagnosis) {
        if (diagnosis.clientCI == null || diagnosis.motorcycleLicensePlate == null || diagnosis.employeeCI == null) {
            Log.e(tag, "Cannot create spare parts: Missing required fields")
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
                .setTitle("Continuar con Repuestos")
                .setMessage("¿Desea notificar al cliente sobre el diagnóstico?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithSpareParts(diagnosis, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithSpareParts(diagnosis, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithSpareParts(diagnosis: Diagnosis, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        val spareParts = SparePartsPost(
            date = currentDate,
            clientCI = diagnosis.clientCI,
            motorcycleLicensePlate = diagnosis.motorcycleLicensePlate,
            employeeCI = diagnosis.employeeCI,
            listSpareParts = null
        )

        sparePartsRepository.createSpareParts(spareParts).enqueue(object : Callback<SparePartsPost> {
            override fun onResponse(call: Call<SparePartsPost>, response: Response<SparePartsPost>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Spare Parts created successfully for diagnosis: ${diagnosis.id}")
                    diagnosis.id?.let { id ->
                        updateDiagnosisReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Repuestos creados exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (notifyClient) {
                        diagnosis.clientCI?.let { ci ->
                            getClient.getClientById(
                                ci = ci,
                                onSuccess = { client ->
                                    currentClient = client
                                    val notification = MessageNotification(
                                        message = "${getGender(client.gender)} ${client.name + client.lastName} su moto termino la fase de diagnostico. \nSe le informara sobre los repuestos que se llegaran a necesitar."
                                    )
                                    messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                            if (response.isSuccessful) {
                                                Log.d(tag, "Notification sent successfully for diagnosis: ${diagnosis.id}")
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
                    val errorMsg = "Error al crear repuestos: ${response.code()} ${response.message()}"
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

            override fun onFailure(call: Call<SparePartsPost>, t: Throwable) {
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

    private fun updateDiagnosisReviewedStatus(id: String, reviewed: Boolean) {
        diagnosisRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Diagnosis $id marked as reviewed=$reviewed")
                    viewModel.fetchDiagnosis(1, 100, token) // Usar el token proporcionado
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Estado actualizado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(tag, "Failed to update diagnosis reviewed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado de diagnóstico: ${response.message()}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(tag, "Error updating diagnosis reviewed status: ${t.message}", t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        "Error de conexión al actualizar diagnóstico: ${t.message}",
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