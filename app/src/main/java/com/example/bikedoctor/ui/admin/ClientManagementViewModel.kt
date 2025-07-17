package com.example.bikedoctor.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Client
import com.example.bikedoctor.data.repository.ClientRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientManagementViewModel : ViewModel() {

    private val repository = ClientRepository()
    private val _clients = MutableLiveData<List<Client>>()
    val clients: LiveData<List<Client>> = _clients

    init {
        loadClients()
    }

    fun loadClients() {
        repository.getClients().enqueue(object : Callback<List<Client>> {
            override fun onResponse(call: Call<List<Client>>, response: Response<List<Client>>) {
                if (response.isSuccessful) {
                    _clients.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<Client>>, t: Throwable) {
                // Handle error
            }
        })
    }
}