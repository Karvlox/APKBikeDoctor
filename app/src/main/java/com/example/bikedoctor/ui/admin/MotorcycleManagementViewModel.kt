package com.example.bikedoctor.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.Motorcycle
import com.example.bikedoctor.data.repository.MotorcycleRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MotorcycleManagementViewModel : ViewModel() {

    private val repository = MotorcycleRepository()
    private val _motorcycles = MutableLiveData<List<Motorcycle>>()
    val motorcycles: LiveData<List<Motorcycle>> = _motorcycles

    init {
        loadMotorcycles()
    }

    fun loadMotorcycles() {
        repository.getMotorcycles().enqueue(object : Callback<List<Motorcycle>> {
            override fun onResponse(call: Call<List<Motorcycle>>, response: Response<List<Motorcycle>>) {
                if (response.isSuccessful) {
                    _motorcycles.value = response.body()
                }
            }

            override fun onFailure(call: Call<List<Motorcycle>>, t: Throwable) {
                // Handle error
            }
        })
    }
}