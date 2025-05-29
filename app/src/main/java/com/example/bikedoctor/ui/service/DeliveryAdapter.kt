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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.repository.DeliveryRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class DeliveryAdapter(context: Context, delivery: List<Delivery>) :
    ArrayAdapter<Delivery>(context, 0, delivery) {

    private val tag = "DeliveryAdapter"
    private val deliveryRepository = DeliveryRepository()
    private val messageNotificationRepository = MessageNotificationRepository()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_item_service, parent, false)

        val delivery = getItem(position)
        if (delivery == null) {
            Log.e(tag, "Delivery at position $position is null")
            return view
        }

        Log.d(tag, "Rendering delivery: id=${delivery.id}")

        val idServiceText = view.findViewById<TextView>(R.id.idService)
        val nameCIText = view.findViewById<TextView>(R.id.clientCI)
        val motorcycleClientText = view.findViewById<TextView>(R.id.motorcycleLicensePlate)
        val employeeCIText = view.findViewById<TextView>(R.id.employeeCI)
        val firstReasonText = view.findViewById<TextView>(R.id.details)

        idServiceText.text = delivery.id ?: "Sin ID"
        nameCIText.text = "Cliente: ${delivery.clientCI ?: "Desconocido"}"
        motorcycleClientText.text = "Motocicleta: ${delivery.motorcycleLicensePlate ?: "Sin datos"}"
        employeeCIText.text = "Empleado Reponsable: ${delivery.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Formulario llenado?: ${delivery.surveyCompleted?: "Sin confirmación"}"

        // Hacer que el botón editButtom sea invisible
        view.findViewById<ImageView>(R.id.editButtom).visibility = View.INVISIBLE // o View.INVISIBLE según prefieras

        // Configurar botones (placeholders)
        view.findViewById<ImageView>(R.id.editButtom)?.setOnClickListener {
            Log.d(tag, "Edit button clicked for reception: ${delivery.id}")
            // TODO: Implementar acción de edición
        }
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for reception: ${delivery.id}")
            // TODO: Implementar acción de continuación
            finishFromDelivery(delivery)
        }

        return view
    }

    private fun finishFromDelivery(delivery : Delivery){
        if (delivery.clientCI == null || delivery.motorcycleLicensePlate == null || delivery.employeeCI == null) {
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
                    proceedWithReparation(delivery, notifyClient = true)
                }
                .setNegativeButton("Continuar sin Notificar") { _, _ ->
                    proceedWithReparation(delivery, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithReparation(delivery: Delivery, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        delivery.id?.let { id ->
            updateDeliveryReviewedStatus(id, true)
            updateDeliverySurveyCompletedStatus(id, true)
        }

        // If notifyClient is true, send notification
        if (notifyClient) {
            val notification = MessageNotification(
                message = "Enviar formulario (${delivery.clientCI}) (${delivery.motorcycleLicensePlate}) en la fecha $currentDate."
            )
            messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(tag, "Notification sent successfully for reception: ${delivery.id}")
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
    }

    private fun updateDeliveryReviewedStatus(id: String, reviewed: Boolean) {
        deliveryRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Delivery $id marked as reviewed=$reviewed")
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

    private fun updateDeliverySurveyCompletedStatus(id: String, reviewed: Boolean) {
        deliveryRepository.updateSurveyCompletedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Control $id marked as reviewed=$reviewed")
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