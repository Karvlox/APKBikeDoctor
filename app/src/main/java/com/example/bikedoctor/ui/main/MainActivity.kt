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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(SessionRepository(applicationContext))
    }
    private val sessionViewModel: SessionViewModel by viewModels {
        SessionViewModelFactory(SessionRepository(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar que los elementos del layout existan
        if (binding.frameLayout == null || binding.bottomNavigationView2 == null) {
            throw IllegalStateException("Layout de MainActivity no contiene frame_layout o bottomNavigationView2")
        }

        // Configurar BottomNavigationView
        binding.bottomNavigationView2.setOnItemSelectedListener { menuItem ->
            mainViewModel.onNavigationItemSelected(menuItem.itemId)
            true
        }

        // Observar el estado de navegación
        mainViewModel.navigationState.observe(this) { state ->
            replaceFragment(state.selectedItemId)
        }

        // Observar el token para verificar la sesión
        sessionViewModel.token.observe(this) { token ->
            if (token == null) {
                // Si no hay token, redirigir al usuario a la pantalla de login
                // Por ejemplo, iniciar SignIn activity y finalizar esta
                // startActivity(Intent(this, SignIn::class.java))
                // finish()
            } else {
                // Token disponible, continuar con la lógica normal
                if (savedInstanceState == null) {
                    replaceFragment(R.id.home)
                }
            }
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

class SessionViewModelFactory(private val sessionRepository: SessionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(sessionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}