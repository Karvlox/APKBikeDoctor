package com.example.bikedoctor.ui.signUp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bikedoctor.data.model.StaffLogin
import com.example.bikedoctor.data.model.StaffPost
import com.example.bikedoctor.data.remote.LoginResponse
import com.example.bikedoctor.data.repository.StaffRepository
import com.example.bikedoctor.data.repository.SessionRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpViewModel(
    private val staffRepository: StaffRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> get() = _registerState

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    fun register(name: String, lastName: String, ci: Int, password: String, age: Int, numberPhone: Int, role: String) {
        val staffPost = StaffPost(name, lastName, ci, password, age, numberPhone, role)
        staffRepository.registerStaff(staffPost).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val rawResponse = response.body()?.string() ?: "Cuerpo vacío"
                if (response.isSuccessful) {
                    _registerState.value = RegisterState.Success
                    loginAfterRegister(ci, password)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("SignUpViewModel", "Error: ${response.code()} - ${response.message()} - $errorBody")
                    _registerState.value = RegisterState.Error("Error: ${response.code()} - $errorBody")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _registerState.value = RegisterState.Error("Error de red: ${t.message}")
            }
        })
    }

    private fun loginAfterRegister(ci: Int, password: String) {
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
                    Log.e("SignUpViewModel", "Error en la respuesta de login: ${response.code()} - ${response.message()}")
                    _loginState.value = LoginState.Error("Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("SignUpViewModel", "Error de red en login: ${t.message}", t)
                _loginState.value = LoginState.Error("Error de red: ${t.message}")
            }
        })
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}

sealed class LoginState {
    object Idle : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}