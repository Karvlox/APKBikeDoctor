package com.example.bikedoctor.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.example.bikedoctor.ui.signInUp.PrivacyPolicy
import com.example.bikedoctor.ui.signInUp.SignInUP
import com.example.bikedoctor.utils.ClientValidator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class SignUp : AppCompatActivity() {

    private val viewModel: SignUpViewModel by viewModels {
        SignUpViewModelFactory(StaffRepository(), SessionRepository(this))
    }

    private val privacyPolicyLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        // No necesitamos manejar el resultado, simplemente regresamos a SignUp
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
        val privacyPolicyCheckBox: CheckBox = findViewById(R.id.privacyPolicyCheckBox)
        val privacyPolicyText: TextView = findViewById(R.id.textView53)

        // Restaurar datos si existen
        savedInstanceState?.let {
            nameInput.setText(it.getString("name"))
            lastNameInput.setText(it.getString("lastName"))
            ciInput.setText(it.getString("ci"))
            ageInput.setText(it.getString("age"))
            numberPhoneInput.setText(it.getString("numberPhone"))
            passwordInput.setText(it.getString("password"))
            val roleIndex = it.getInt("roleIndex", 0)
            spinner.setSelection(roleIndex)
            privacyPolicyCheckBox.isChecked = it.getBoolean("privacyPolicyChecked")
        }

        backButton.setOnClickListener {
            val intent = Intent(this, SignInUP::class.java)
            startActivity(intent)
            finish()
        }

        // Navegar a PrivacyPolicy al hacer clic en el texto
        privacyPolicyText.setOnClickListener {
            val intent = Intent(this, PrivacyPolicy::class.java)
            privacyPolicyLauncher.launch(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val opciones = arrayOf("Selecciona una opción", "ADMIN", "EMPLEADO")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            opciones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

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

        registerButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val ciText = ciInput.text.toString().trim()
            val ageText = ageInput.text.toString().trim()
            val numberPhoneText = numberPhoneInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val role = spinner.selectedItem.toString()

            val nameError = ClientValidator.validateName(name)
            val lastNameError = ClientValidator.validateLastName(lastName)
            val ciError = ClientValidator.validateCI(ciText)
            val ageError = ClientValidator.validateAge(ageText)
            val phoneError = ClientValidator.validatePhone(numberPhoneText)
            val passwordError = if (password.length < 6) "La contraseña debe tener al menos 6 caracteres" else null
            val privacyError = if (!privacyPolicyCheckBox.isChecked) "Debes aceptar la Política de Privacidad" else null

            nameInputLayout.error = nameError
            lastNameInputLayout.error = lastNameError
            ciInputLayout.error = ciError
            ageInputLayout.error = ageError
            numberPhoneInputLayout.error = phoneError
            passwordInputLayout.error = passwordError

            if (nameError != null || lastNameError != null || ciError != null || ageError != null ||
                phoneError != null || passwordError != null || role == "Selecciona una opción" || privacyError != null) {
                if (role == "Selecciona una opción") {
                    showToast("Seleccione un rol válido")
                }
                if (privacyError != null) {
                    showToast(privacyError)
                }
                return@setOnClickListener
            }

            val ci = ciText.toIntOrNull()
            val age = ageText.toIntOrNull()
            val numberPhone = numberPhoneText.toIntOrNull()
            if (ci == null || age == null || numberPhone == null) {
                showToast("Error en la conversión de datos")
                return@setOnClickListener
            }

            viewModel.register(name, lastName, ci, password, age, numberPhone, role)
        }

        signInButton.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Success -> {
                    Toast.makeText(this, "Registro exitoso, iniciando sesión...", Toast.LENGTH_SHORT).show()
                }
                is RegisterState.Error -> {
                    showToast(state.message)
                }
                is RegisterState.Idle -> {
                }
            }
        }

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
                    showToast("Error al iniciar sesión: ${state.message}")
                }
                is LoginState.Idle -> {
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val nameInput: TextInputEditText = findViewById(R.id.textInputNameEdit)
        val lastNameInput: TextInputEditText = findViewById(R.id.textInputLastNameEdit)
        val ciInput: TextInputEditText = findViewById(R.id.textInputCiEdit)
        val ageInput: TextInputEditText = findViewById(R.id.textInputAgeEdit)
        val numberPhoneInput: TextInputEditText = findViewById(R.id.textInputNumberPhoneEdit)
        val passwordInput: TextInputEditText = findViewById(R.id.textInputPasswordEdit)
        val spinner: Spinner = findViewById(R.id.filterSpinner)
        val privacyPolicyCheckBox: CheckBox = findViewById(R.id.privacyPolicyCheckBox)

        outState.putString("name", nameInput.text.toString())
        outState.putString("lastName", lastNameInput.text.toString())
        outState.putString("ci", ciInput.text.toString())
        outState.putString("age", ageInput.text.toString())
        outState.putString("numberPhone", numberPhoneInput.text.toString())
        outState.putString("password", passwordInput.text.toString())
        outState.putInt("roleIndex", spinner.selectedItemPosition)
        outState.putBoolean("privacyPolicyChecked", privacyPolicyCheckBox.isChecked)
    }

    private fun showToast(message: String) {
        val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        toast.show()
        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 3000)
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