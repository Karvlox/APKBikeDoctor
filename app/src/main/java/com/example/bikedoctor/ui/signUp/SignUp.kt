package com.example.bikedoctor.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
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
import com.example.bikedoctor.ui.signIn.SignIn
import com.example.bikedoctor.ui.signInUp.SignInUP
import com.example.bikedoctor.utils.ClientValidator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUp : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels {
        SignUpViewModelFactory(StaffRepository(), SessionRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        val spinner: Spinner = findViewById(R.id.filterSpinner)
        val nameInputLayout: TextInputLayout = findViewById(R.id.textInputName)
        val lastNameInputLayout: TextInputLayout = findViewById(R.id.textInputLastName)
        val ciInputLayout: TextInputLayout = findViewById(R.id.textInputCI)
        val ageInputLayout: TextInputLayout = findViewById(R.id.textInputAge)
        val numberPhoneInputLayout: TextInputLayout = findViewById(R.id.textInputNumberPhone)
        val passwordInputLayout: TextInputLayout = findViewById(R.id.textInputPassword)
        val nameInput: TextInputEditText = findViewById(R.id.textInputNameEdit)
        val lastNameInput: TextInputEditText = findViewById(R.id.textInputLastNameEdit)
        val ciInput: TextInputEditText = findViewById(R.id.textInputCiEdit)
        val ageInput: TextInputEditText = findViewById(R.id.textInputAgeEdit)
        val numberPhoneInput: TextInputEditText = findViewById(R.id.textInputNumberPhoneEdit)
        val passwordInput: TextInputEditText = findViewById(R.id.textInputPasswordEdit)
        val registerButton: TextView = findViewById(R.id.buttonRegister)
        val signInButton: TextView = findViewById(R.id.buttonSignIn)
        val backButton: ImageView = findViewById(R.id.backButtom)

        backButton.setOnClickListener {
            val intent = Intent(this, SignInUP::class.java)
            startActivity(intent)
            finish()
        }

        // Configurar el padding para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar el spinner
        val opciones = arrayOf("Selecciona una opción", "ADMIN", "EMPLEADO")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Configurar el toggle de visibilidad de la contraseña
        passwordInputLayout.setEndIconOnClickListener {
            val editText = passwordInputLayout.editText ?: return@setEndIconOnClickListener
            val isPasswordVisible = (editText.inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.inputType = if (isPasswordVisible) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
            Toast.makeText(
                this,
                if (isPasswordVisible) "Contraseña oculta" else "Contraseña visible",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Configurar el botón de registro
        registerButton.setOnClickListener {
            // Obtener valores de los inputs
            val name = nameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val ciText = ciInput.text.toString().trim()
            val ageText = ageInput.text.toString().trim()
            val numberPhoneText = numberPhoneInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val role = spinner.selectedItem.toString()

            // Validar inputs con ClientValidator y mostrar errores en los inputs
            val nameError = ClientValidator.validateName(name)
            val lastNameError = ClientValidator.validateLastName(lastName)
            val ciError = ClientValidator.validateCI(ciText)
            val ageError = ClientValidator.validateAge(ageText)
            val phoneError = ClientValidator.validatePhone(numberPhoneText)
            val passwordError = if (password.length < 6) "La contraseña debe tener al menos 6 caracteres" else null

            nameInputLayout.error = nameError
            lastNameInputLayout.error = lastNameError
            ciInputLayout.error = ciError
            ageInputLayout.error = ageError
            numberPhoneInputLayout.error = phoneError
            passwordInputLayout.error = passwordError

            // Verificar si hay errores de validación
            if (nameError != null || lastNameError != null || ciError != null || ageError != null ||
                phoneError != null || passwordError != null || role == "Selecciona una opción") {
                if (role == "Selecciona una opción") {
                    showToast("Seleccione un rol válido")
                }
                return@setOnClickListener
            }

            // Si las validaciones locales pasan, intentar el registro
            val ci = ciText.toIntOrNull()
            val age = ageText.toIntOrNull()
            val numberPhone = numberPhoneText.toIntOrNull()
            if (ci == null || age == null || numberPhone == null) {
                showToast("Error en la conversión de datos")
                return@setOnClickListener
            }

            viewModel.register(name, lastName, ci, password, age, numberPhone, role)
        }

        // Configurar el botón de iniciar sesión
        signInButton.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }

        // Observar el estado de registro
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Success -> {
                    Toast.makeText(this, "Registro exitoso, iniciando sesión...", Toast.LENGTH_SHORT).show()
                }
                is RegisterState.Error -> {
                    showToast(state.message)
                }
                is RegisterState.Idle -> {
                    // No hacer nada
                }
            }
        }

        // Observar el estado de login tras registro
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Success -> {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    showToast("Error al iniciar sesión: ${state.message}")
                }
                is LoginState.Idle -> {
                    // No hacer nada
                }
            }
        }
    }

    // Función para mostrar un Toast que desaparece después de 3 segundos
    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 3000) // Desaparece después de 3 segundos
    }
}

class SignUpViewModelFactory(
    private val staffRepository: StaffRepository,
    private val sessionRepository: SessionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignUpViewModel(staffRepository, sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}