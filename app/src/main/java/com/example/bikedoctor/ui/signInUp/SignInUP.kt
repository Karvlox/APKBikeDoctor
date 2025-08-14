package com.example.bikedoctor.ui.signInUp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bikedoctor.R
import com.example.bikedoctor.ui.signIn.SignIn
import com.example.bikedoctor.ui.signUp.SignUp

class SignInUP : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        try {
            setContentView(R.layout.activity_sign_in_up)
        } catch (e: Exception) {
            Log.e("SignInUP", "Error al cargar el layout: ${e.message}")
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // "Iniciar Sesión"
        val textViewIniciarSesion: TextView = findViewById(R.id.buttonSignIn)
        textViewIniciarSesion.setOnClickListener {
            try {
                val intent = Intent(this, SignIn::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("SignInUP", "Error al iniciar SignIn: ${e.message}")
            }
        }

        // "Registrarse"
        val textViewRegistrarse: TextView = findViewById(R.id.buttonRegister)
        textViewRegistrarse.setOnClickListener {
            try {
                val intent = Intent(this, SignUp::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("SignInUP", "Error al iniciar SignUp: ${e.message}")
            }
        }
    }
}