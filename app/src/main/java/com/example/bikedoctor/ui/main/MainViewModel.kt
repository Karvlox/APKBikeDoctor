package com.example.bikedoctor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bikedoctor.R
import com.example.bikedoctor.data.model.NavigationState
import com.example.bikedoctor.data.repository.SessionRepository

class MainViewModel(private val sessionRepository: SessionRepository) : ViewModel() {

    private val _navigationState = MutableLiveData<NavigationState>()
    val navigationState: LiveData<NavigationState> get() = _navigationState

    init {
        // Estado inicial: seleccionar Home
        _navigationState.value = NavigationState(R.id.home)
    }

    fun onNavigationItemSelected(itemId: Int) {
        _navigationState.value = NavigationState(itemId)
    }
}