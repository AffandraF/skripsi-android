package com.example.skripsi.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.skripsi.databinding.ActivityRegisterBinding
import com.example.skripsi.R
import com.example.skripsi.viewmodel.AuthState
import com.example.skripsi.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Google Sign-In
        googleSignInClient = GoogleSignIn.getClient(this, authViewModel.getGoogleSignInOptions())

        // Observing auth state changes
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // Show loading indicator
                    binding.progressBar.visibility = View.VISIBLE
                }
                is AuthState.Success -> {
                    // Navigate to main screen or show success message
                    binding.progressBar.visibility = View.GONE
                }
                is AuthState.Error -> {
                    // Show error message
                    binding.progressBar.visibility = View.GONE
                    Log.e(TAG, "Registration failed: ${state.message}")
                }
                else -> {
                    // Handle any unexpected state
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        binding.buttonRegister.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.registerWithEmail(email, password)
            }
        }

        binding.buttonGoogleRegister.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1001)  // You can handle the result in onActivityResult
        }

        // Navigate to LoginActivity when the user already has an account
        binding.linkToLogin.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                account?.idToken?.let { idToken ->
                    authViewModel.loginWithGoogle(idToken)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google registration failed", e)
            }
        }
    }
}
