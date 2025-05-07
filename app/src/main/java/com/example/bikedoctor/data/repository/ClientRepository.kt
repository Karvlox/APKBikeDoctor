package com.example.bikedoctor.data.repository

import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.remote.RetrofitClient
import retrofit2.Call

class ClientRepository {
    fun registerClient(client: Client): Call<Client> {
        return RetrofitClient.clientApi.registerClient(client)
    }

    fun getClients(): Call<List<Client>> {
        return RetrofitClient.clientApi.getClients()
    }
}