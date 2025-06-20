package com.example.bikedoctor.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bikedoctor.R
import com.example.bikedoctor.data.repository.SessionRepository
import com.example.bikedoctor.data.remote.RetrofitClient
import com.example.bikedoctor.databinding.ActivityMainBinding
import com.example.bikedoctor.ui.home.HomeFragment
import com.example.bikedoctor.ui.profile.ProfileFragment
import com.example.bikedoctor.ui.service.TableWorkFragment
import com.example.bikedoctor.ui.signIn.SignIn
import kotlinx.coroutines.runBlocking

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

        if (binding.frameLayout == null || binding.bottomNavigationView2 == null) {
            throw IllegalStateException("Layout de MainActivity no contiene frame_layout o bottomNavigationView2")
        }

        binding.bottomNavigationView2.setOnItemSelectedListener { menuItem ->
            mainViewModel.onNavigationItemSelected(menuItem.itemId)
            true
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

        sessionViewModel.token.observe(this) { token ->
            RetrofitClient.updateToken(token)
            if (token == null) {
                startActivity(Intent(this, SignIn::class.java))
                finish()
            } else {
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
            else -> HomeFragment()
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