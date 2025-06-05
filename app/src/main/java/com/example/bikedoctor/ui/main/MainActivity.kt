package com.example.bikedoctor.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bikedoctor.R
import com.example.bikedoctor.data.repository.SessionRepository
import com.example.bikedoctor.databinding.ActivityMainBinding
import com.example.bikedoctor.ui.home.HomeFragment
import com.example.bikedoctor.ui.profile.ProfileFragment
import com.example.bikedoctor.ui.service.TableWorkFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(SessionRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar que los elementos del layout existan
        if (binding.frameLayout == null || binding.bottomNavigationView2 == null) {
            throw IllegalStateException("Layout de MainActivity no contiene frame_layout o bottomNavigationView2")
        }

        // Obtener el token del Intent
        val token = intent.getStringExtra("USER_TOKEN")
        if (token != null) {
            viewModel.setToken(token) // Pasar el token al ViewModel
        }

        // Configurar BottomNavigationView
        binding.bottomNavigationView2.setOnItemSelectedListener { menuItem ->
            viewModel.onNavigationItemSelected(menuItem.itemId)
            true
        }

        // Observar el estado de navegación
        viewModel.navigationState.observe(this) { state ->
            replaceFragment(state.selectedItemId)
        }

        // Inicializar el fragmento inicial si no hay estado guardado
        if (savedInstanceState == null) {
            replaceFragment(R.id.home)
        }
    }

    private fun replaceFragment(itemId: Int) {
        val fragment = when (itemId) {
            R.id.home -> HomeFragment()
            R.id.table -> TableWorkFragment()
            R.id.profile -> ProfileFragment()
            else -> HomeFragment() // Valor por defecto
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
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