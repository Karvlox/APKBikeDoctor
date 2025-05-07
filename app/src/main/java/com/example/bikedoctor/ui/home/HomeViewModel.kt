package com.example.bikedoctor.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.data.model.HomeData
import com.example.bikedoctor.data.repository.HomeRepository

class HomeViewModel : ViewModel() {

    private val repository = HomeRepository()

    private val _homeData = MutableLiveData<HomeData>()
    val homeData: LiveData<HomeData> = _homeData

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        _homeData.value = repository.getHomeData()
    }
}