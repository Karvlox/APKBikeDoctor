package com.example.bikedoctor.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bikedoctor.data.repository.SessionRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SessionViewModel(private val sessionRepository: SessionRepository) : ViewModel() {
    private val _token = MutableLiveData<String?>()
    val token: LiveData<String?> get() = _token

    init {
        viewModelScope.launch {
            sessionRepository.token.collect { token ->
                _token.value = token
            }
        }
    }

    fun setToken(token: String) {
        viewModelScope.launch {
            sessionRepository.saveToken(token)
        }
    }

    fun clearToken() {
        viewModelScope.launch {
            sessionRepository.clearToken()
        }
    }
}