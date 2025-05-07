package com.example.bikedoctor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.NavigationState

class MainViewModel : ViewModel() {

    private val _navigationState = MutableLiveData<NavigationState>()
    val navigationState: LiveData<NavigationState> = _navigationState

    init {
        // Estado inicial: seleccionar Home
        _navigationState.value = NavigationState(R.id.home)
    }

    fun onNavigationItemSelected(itemId: Int) {
        _navigationState.value = NavigationState(itemId)
    }
}