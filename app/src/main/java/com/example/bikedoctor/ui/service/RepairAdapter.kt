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
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.model.QualityControlPost
import com.example.bikedoctor.data.model.Repair
import com.example.bikedoctor.data.repository.ControlRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.data.repository.RepairRepository
import com.example.bikedoctor.utils.ParserHour
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class RepairAdapter(context: Context, repair: List<Repair>) :
    ArrayAdapter<Repair>(context, 0, repair) {

    private val tag = "RepairAdapter"
    private val controlRepository = ControlRepository()
    private val repairRepository = RepairRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val parseHour = ParserHour()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val repair = getItem(position)
        if (repair == null) {
            Log.e(tag, "Repairs at position $position is null")
            return view
        }

        Log.d(tag, "Rendering repairs: id=${repair.id}")

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
        employeeCIText.text = "Empleado Reponsable: ${repair.employeeCI ?: "Sin datos"}"
        firstReasonText.text = if (firstReparation != null) {
            "Lista de Reparacion: ${firstReparation.nameReparation}"
        } else {
            "Sin reparacion especificados"
        }

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${repair.id}")
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val bundle = bundleOf(
                "repair_id" to repair.id,
                "repair_date" to parseHour.parserHourService(repair.date.toString()),
                "repair_clientCI" to repair.clientCI?.toString(),
                "repair_motorcycleLicensePlate" to repair.motorcycleLicensePlate,
                "repair_employeeCI" to repair.employeeCI?.toString(),
                "repair_listDiagnostic" to repair.listReparations?.toTypedArray(),
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
            Log.d(tag, "Continue button clicked for reception: ${repair.id}")
            createControlFromRepair(repair)
        }

        return view
    }

    private fun createControlFromRepair(repair: Repair){
        if (repair.clientCI == null || repair.motorcycleLicensePlate == null || repair.employeeCI == null) {
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
                .setTitle("Continuar con Reparación")
                .setMessage("¿Notificar la Aprobación de Costos?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithSpareParts(repair, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithSpareParts(repair, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithSpareParts(repair: Repair, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        // Create DiagnosisPost object
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
                    Log.d(tag, "Repair created successfully for reception: ${repair.id}")
                    repair.id?.let { id ->
                        updateSparePartReviewedStatus(id, true)
                    }
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Control de Calidad creado exitosamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }

                    // If notifyClient is true, send notification
                    if (notifyClient) {
                        val notification = MessageNotification(
                            message = "Se ha creado el control de calidad para su motocicleta (${repair.motorcycleLicensePlate}) en la fecha $currentDate."
                        )
                        messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d(tag, "Notification sent successfully for reception: ${repair.id}")
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

    private fun updateSparePartReviewedStatus(id: String, reviewed: Boolean) {
        repairRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Repair $id marked as reviewed=$reviewed")
                    (context as? FragmentActivity)?.run {
                        val viewModel = ViewModelProvider(this)
                            .get(RepairViewModel::class.java)
                        viewModel.fetchCards(1, 100)
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
}