package com.example.bikedoctor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.bikedoctor.ui.signInUp.SignInUP

class LoardingPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loarding_page)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, SignInUP::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}