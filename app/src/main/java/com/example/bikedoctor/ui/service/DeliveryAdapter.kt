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
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.model.Delivery
import com.example.bikedoctor.data.model.MessageNotification
import com.example.bikedoctor.data.repository.DeliveryRepository
import com.example.bikedoctor.data.repository.MessageNotificationRepository
import com.example.bikedoctor.utils.GetClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DeliveryAdapter(
    context: Context,
    deliveries: List<Delivery>,
    private val viewModel: DeliveryViewModel,
    private val token: String?
) : ArrayAdapter<Delivery>(context, 0, deliveries) {

    private val tag = "DeliveryAdapter"
    private val deliveryRepository = DeliveryRepository()
    private val messageNotificationRepository = MessageNotificationRepository()
    private val getClient = GetClient(context)
    private var currentClient: Client? = null

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
        employeeCIText.text = "Empleado Responsable: ${delivery.employeeCI ?: "Sin datos"}"
        firstReasonText.text = "Encuesta completada: ${if (delivery.surveyCompleted == true) "Sí" else "No"}"

        // Mantener el botón de edición invisible
        view.findViewById<ImageView>(R.id.editButtom)?.visibility = View.INVISIBLE

        // Configurar botón de continuación
        view.findViewById<ImageView>(R.id.continueBottom)?.setOnClickListener {
            Log.d(tag, "Continue button clicked for delivery: ${delivery.id}")
            completeDelivery(delivery)
        }

        return view
    }

    private fun completeDelivery(delivery: Delivery) {
        if (delivery.clientCI == null || delivery.motorcycleLicensePlate == null || delivery.employeeCI == null) {
            Log.e(tag, "Cannot complete delivery: Missing required fields")
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
                .setTitle("Finalizar Entrega")
                .setMessage("¿Desea notificar al cliente con un enlace a la encuesta de satisfacción?")
                .setPositiveButton("Notificar al Cliente") { _, _ ->
                    proceedWithDelivery(delivery, notifyClient = true)
                }
                .setNegativeButton("Finalizar sin Notificar") { _, _ ->
                    proceedWithDelivery(delivery, notifyClient = false)
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun proceedWithDelivery(delivery: Delivery, notifyClient: Boolean) {
        val calendar = Calendar.getInstance()
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        outputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = outputFormat.format(calendar.time)

        delivery.id?.let { id ->
            updateDeliverySurveyCompletedStatus(id, true)
            updateDeliveryReviewedStatus(id, true)
        }

        if (notifyClient) {
            delivery.clientCI?.let { ci ->
                getClient.getClientById(
                    ci = ci,
                    onSuccess = { client ->
                        currentClient = client
                        val notification = MessageNotification(
                            message = "${getGender(client.gender)} ${client.name} ${client.lastName}, ¡gracias por elegir BikeDoctor! Su motocicleta ha sido entregada. " +
                                    "Por favor, complete nuestra encuesta de satisfacción: " +
                                    "https://docs.google.com/forms/d/e/1FAIpQLSeTAR_GttK8qWzX9ouf0N1_ao6NGRaw-UFS7VlGPhvZ68Oxxg/viewform"
                        )
                        messageNotificationRepository.sendNotification(notification).enqueue(object : Callback<Void> {
                            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                                if (response.isSuccessful) {
                                    Log.d(tag, "Notification sent successfully for delivery: ${delivery.id}")
                                    (context as? FragmentActivity)?.run {
                                        android.widget.Toast.makeText(
                                            this,
                                            "Entrega finalizada y notificación enviada",
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
        } else {
            (context as? FragmentActivity)?.run {
                android.widget.Toast.makeText(
                    this,
                    "Entrega finalizada",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateDeliveryReviewedStatus(id: String, reviewed: Boolean) {
        deliveryRepository.updateReviewedStatus(id, reviewed).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Delivery $id marked as reviewed=$reviewed")
                    viewModel.fetchDeliveries(1, 100, token) // Usar el token proporcionado
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Estado de entrega actualizado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(tag, "Failed to update delivery reviewed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado de entrega: ${response.message()}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(tag, "Error updating delivery reviewed status: ${t.message}", t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        "Error de conexión al actualizar entrega: ${t.message}",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun updateDeliverySurveyCompletedStatus(id: String, surveyCompleted: Boolean) {
        deliveryRepository.updateSurveyCompletedStatus(id, surveyCompleted).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(tag, "Delivery $id marked as surveyCompleted=$surveyCompleted")
                    viewModel.fetchDeliveries(1, 100, token) // Usar el token proporcionado
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Estado de encuesta actualizado correctamente",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(tag, "Failed to update delivery survey completed status: ${response.code()} ${response.message()}")
                    (context as? FragmentActivity)?.run {
                        android.widget.Toast.makeText(
                            this,
                            "Error al actualizar estado de encuesta: ${response.message()}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(tag, "Error updating delivery survey completed status: ${t.message}", t)
                (context as? FragmentActivity)?.run {
                    android.widget.Toast.makeText(
                        this,
                        "Error de conexión al actualizar encuesta: ${t.message}",
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