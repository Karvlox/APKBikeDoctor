package com.example.bikedoctor.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.repository.ClientRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GetClient(private val context: Context) {
    private val clientRepository = ClientRepository()
    private val tag = "GetClient"

    fun getClientById(ci: Int, onSuccess: (Client) -> Unit, onError: (String) -> Unit) {
        clientRepository.getClientById(ci).enqueue(object : Callback<Client> {
            override fun onResponse(call: Call<Client>, response: Response<Client>) {
                if (response.isSuccessful) {
                    response.body()?.let { client ->
                        Log.d(tag, "Client fetched successfully: $client")
                        onSuccess(client)
                    } ?: run {
                        Log.e(tag, "Client not found for CI: $ci")
                        onError("Client not found")
                        showToast("Cliente no encontrado")
                    }
                } else {
                    val errorMessage = "Failed to fetch client: ${response.code()} ${response.message()}"
                    Log.e(tag, errorMessage)
                    onError(errorMessage)
                    showToast("Error al obtener cliente: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Client>, t: Throwable) {
                val errorMessage = "Network error: ${t.message}"
                Log.e(tag, errorMessage, t)
                onError(errorMessage)
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        (context as? FragmentActivity)?.run {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}