package com.example.bikedoctor.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.bikedoctor.R
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import com.example.bikedoctor.ui.main.MainActivity
import com.google.android.material.textfield.TextInputLayout

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Configurar el padding para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener referencia al Spinner
        val spinner: Spinner = findViewById(R.id.filterSpinner)
        val passwordInputLayout: TextInputLayout = findViewById(R.id.textInputPassword)

        // Crear una lista de opciones para el Spinner
        val opciones = arrayOf("Selecciona una opción", "Administrador", "Empleado")

        // Configurar el ArrayAdapter
        val adapter = ArrayAdapter(
            this, // Contexto
            android.R.layout.simple_spinner_item, // Layout predeterminado para los ítems
            opciones // Lista de opciones
        )

        // Especificar el layout para el dropdown
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Asignar el adaptador al Spinner
        spinner.adapter = adapter

        // (Opcional) Manejar la selección del Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                // Acción cuando se selecciona una opción
                val opcionSeleccionada = opciones[position]
                if (position != 0) { // Ignorar la opción por defecto
                    Toast.makeText(this@SignUp, "Seleccionaste: $opcionSeleccionada", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Acción cuando no se selecciona nada
            }
        }

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

        val textViewIniciarSesion: TextView = findViewById(R.id.buttonRegister)
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