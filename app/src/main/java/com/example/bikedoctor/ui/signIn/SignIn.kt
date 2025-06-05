package com.example.bikedoctor.ui.signIn

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bikedoctor.R
import com.google.android.material.textfield.TextInputLayout
import android.text.InputType
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.bikedoctor.ui.main.MainActivity

class SignIn : AppCompatActivity() {
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

        // Obtener referencia al TextInputLayout del password
        val passwordInputLayout: TextInputLayout = findViewById(R.id.textInputPassword)

        // Escuchar clics en el ícono de toggle
        passwordInputLayout.setEndIconOnClickListener {
            // Obtener el EditText dentro del TextInputLayout
            val editText = passwordInputLayout.editText ?: return@setEndIconOnClickListener

            // Verificar si la contraseña está visible
            val isPasswordVisible = (editText.inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            // Mostrar mensaje según el estado actual (antes del cambio)
            Toast.makeText(
                this,
                if (isPasswordVisible) "Contraseña oculta" else "Contraseña visible",
                Toast.LENGTH_SHORT
            ).show()
        }

        val textViewIniciarSesion: TextView = findViewById(R.id.buttonSignIn)
        textViewIniciarSesion.setOnClickListener {
            try {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("SignInUP", "Error al iniciar SignIn: ${e.message}")
            }
        }
    }
}