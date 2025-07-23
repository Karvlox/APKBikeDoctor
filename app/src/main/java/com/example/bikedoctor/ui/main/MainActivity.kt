package com.example.bikedoctor.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bikedoctor.R
import com.example.bikedoctor.data.repository.SessionRepository
import com.example.bikedoctor.data.remote.RetrofitClient
import com.example.bikedoctor.databinding.ActivityMainBinding
import com.example.bikedoctor.ui.aboutUs.AboutUs
import com.example.bikedoctor.ui.home.HomeFragment
import com.example.bikedoctor.ui.learnAboutApp.LearnAboutApp
import com.example.bikedoctor.ui.profile.ProfileFragment
import com.example.bikedoctor.ui.service.TableWorkFragment
import com.example.bikedoctor.ui.signInUp.SignInUP
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(SessionRepository(applicationContext))
    }
    private val sessionViewModel: SessionViewModel by viewModels {
        SessionViewModelFactory(SessionRepository(applicationContext))
    }
    private var tokenObserver: Observer<String?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (binding.frameLayout == null || binding.bottomNavigationView2 == null) {
            throw IllegalStateException("Layout de MainActivity no contiene frame_layout o bottomNavigationView2")
        }

        // Configurar BottomNavigationView
        binding.bottomNavigationView2.setOnItemSelectedListener { menuItem ->
            mainViewModel.onNavigationItemSelected(menuItem.itemId)
            true
        }

        // Configurar botón de logout
        binding.logout.setOnClickListener {
            logout()
        }

        // Configurar botón About Us
        binding.aboutUs.setOnClickListener {
            showAboutUsFragment()
        }

        // Configurar botón Learn About App
        binding.learnAboutApp.setOnClickListener {
            showLearnAboutAppFragment()
        }

        mainViewModel.navigationState.observe(this) { state ->
            replaceFragment(state.selectedItemId)
        }

        // Cargar token desde el Intent
        val tokenFromIntent = intent.getStringExtra("USER_TOKEN")
        if (tokenFromIntent != null) {
            runBlocking {
                SessionRepository(applicationContext).saveToken(tokenFromIntent)
            }
            RetrofitClient.updateToken(tokenFromIntent)
        }

        // Configurar observador de token
        tokenObserver = Observer { token ->
            RetrofitClient.updateToken(token)
            if (token == null && !isFinishing) { // Evitar redirección si la actividad se está cerrando
                startActivity(Intent(this, SignInUP::class.java))
                finish()
            }
        }
        sessionViewModel.token.observe(this, tokenObserver!!)
    }

    private fun replaceFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.home -> HomeFragment()
            R.id.table -> TableWorkFragment()
            R.id.profile -> ProfileFragment()
            else -> HomeFragment()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    private fun logout() {
        runBlocking {
            SessionRepository(applicationContext).clearToken()
        }
        RetrofitClient.updateToken(null)
        tokenObserver?.let { sessionViewModel.token.removeObserver(it) }
        startActivity(Intent(this, SignInUP::class.java))
        finish()
    }

    private fun showAboutUsFragment() {
        val aboutUsFragment = AboutUs()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, aboutUsFragment)
            .addToBackStack("about_us")
            .commit()
    }

    private fun showLearnAboutAppFragment() {
        val learnAboutApp = LearnAboutApp()
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, learnAboutApp)
            .addToBackStack("learn_about_app")
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        tokenObserver?.let { sessionViewModel.token.removeObserver(it) }
    }
}

class MainViewModelFactory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SessionViewModelFactory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}