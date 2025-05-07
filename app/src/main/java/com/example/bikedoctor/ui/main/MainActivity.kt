package com.example.bikedoctor.ui.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bikedoctor.R
import com.example.bikedoctor.databinding.ActivityMainBinding
import com.example.bikedoctor.ui.home.HomeFragment
import com.example.bikedoctor.ui.profile.ProfileFragment
import com.example.bikedoctor.ui.service.TableWorkFragment
import kotlin.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar BottomNavigationView
        binding.bottomNavigationView2.setOnItemSelectedListener {
            viewModel.onNavigationItemSelected(it.itemId)
            true
        }

        // Observar el estado de navegación
        viewModel.navigationState.observe(this) { state ->
            when (state.selectedItemId) {
                R.id.home -> replaceFragment(HomeFragment())
                R.id.table -> replaceFragment(TableWorkFragment())
                R.id.profile -> replaceFragment(ProfileFragment())
            }
        }

        // Cargar HomeFragment por defecto si no hay estado guardado
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}