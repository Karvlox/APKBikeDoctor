package com.example.bikedoctor.ui.signIn

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bikedoctor.R
import com.example.bikedoctor.data.repository.SessionRepository
import com.example.bikedoctor.data.repository.StaffRepository
import com.example.bikedoctor.ui.main.MainActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignIn : AppCompatActivity() {

    private val viewModel: SignInViewModel by viewModels {
        SignInViewModelFactory(StaffRepository(), SessionRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)

        // Configurar el padding para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencias a los elementos de la UI
        val emailInput: TextInputEditText = findViewById(R.id.textInputEmailEditText)
        val passwordInput: TextInputEditText = findViewById(R.id.textInputPasswordEditText)
        val passwordInputLayout: TextInputLayout = findViewById(R.id.textInputPassword)
        val loginButton: TextView = findViewById(R.id.buttonSignIn)

        // Configurar el toggle de visibilidad de la contraseña
        passwordInputLayout.setEndIconOnClickListener {
            val editText = passwordInputLayout.editText ?: return@setEndIconOnClickListener
            val isPasswordVisible = (editText.inputType and android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            Toast.makeText(
                this,
                if (isPasswordVisible) "Contraseña oculta" else "Contraseña visible",
                Toast.LENGTH_SHORT
            ).show()
        }

        loginButton.setOnClickListener {
            val ciText = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            val ci = ciText.toIntOrNull()
            if (ci == null || ci <= 0) {
                Toast.makeText(this, "Cédula inválida", Toast.LENGTH_SHORT).show()
                findViewById<TextInputLayout>(R.id.textInputEmail).error = "Ingrese una cédula válida"
                return@setOnClickListener
            }
            viewModel.login(ci, password)
        }


        // Observar el estado de login
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Success -> {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("USER_TOKEN", state.token)
                    }
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    findViewById<TextInputLayout>(R.id.textInputEmail).error = state.message
                    findViewById<TextInputLayout>(R.id.textInputPassword).error = state.message
                }
                is LoginState.Idle -> {
                    // No hacer nada
                }
            }
        }
    }
}

class SignInViewModelFactory(
    private val staffRepository: StaffRepository,
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignInViewModel(staffRepository, sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}