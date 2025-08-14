package com.example.bikedoctor.ui.signIn

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bikedoctor.data.model.StaffLogin
import com.example.bikedoctor.data.remote.LoginResponse
import com.example.bikedoctor.data.repository.StaffRepository
import com.example.bikedoctor.data.repository.SessionRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInViewModel(
    private val staffRepository: StaffRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(ci: Int, password: String) {
        if (ci <= 0 || password.isEmpty()) {
            _loginState.value = LoginState.Error("Por favor, complete todos los campos")
            return
        }

        val staffLogin = StaffLogin(ci, password)
        staffRepository.loginStaff(staffLogin).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        viewModelScope.launch {
                            sessionRepository.saveToken(token)
                            _loginState.value = LoginState.Success(token)
                        }
                    } else {
                        _loginState.value = LoginState.Error("Error: Token no recibido")
                    }
                } else {
                    Log.e("SignInViewModel", "Error en la respuesta: ${response.code()} - ${response.message()}")
                    _loginState.value = LoginState.Error("Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("SignInViewModel", "Error de red: ${t.message}", t)
                _loginState.value = LoginState.Error("Error de red: ${t.message}")
            }
        })
    }
}

sealed class LoginState {
    object Idle : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}