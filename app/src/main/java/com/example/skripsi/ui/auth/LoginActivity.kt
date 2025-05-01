package com.example.skripsi.ui.auth

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.skripsi.BuildConfig
import com.example.skripsi.databinding.ActivityLoginBinding
import com.example.skripsi.ui.MainActivity
import com.example.skripsi.viewmodel.AuthState
import com.example.skripsi.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val authViewModel: AuthViewModel by viewModels()

    companion object {
        private const val TAG = "LoginActivity"
        private const val REQ_ONE_TAP = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Google One Tap Client
        oneTapClient = Identity.getSignInClient(this)

        // Konfigurasi Google One Tap Sign In
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(BuildConfig.webClientId)
                    .setFilterByAuthorizedAccounts(false) // Agar bisa login dengan akun yang berbeda
                    .build()
            )
            .build()

        setupListeners()
        observeAuthState()
    }

    private fun setupListeners() {
        // Login dengan Email & Password
        binding.buttonLogin.setOnClickListener {
            val email = binding.inputUsername.text.toString()
            val password = binding.inputPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.loginWithEmail(email, password)
            } else {
                Log.e(TAG, "Email or password is empty")
            }
        }

        // Login dengan Google
        binding.buttonGoogleLogin.setOnClickListener {
            oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this) { result ->
                    try {
                        startIntentSenderForResult(
                            result.pendingIntent.intentSender, REQ_ONE_TAP,null,
                            0, 0,
                            0
                        )
                    } catch (e: IntentSender.SendIntentException) {
                        Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Google One Tap Sign-in failed: ${it.localizedMessage}")
                }
        }

        // Navigasi ke Halaman Register
        binding.linkRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun observeAuthState() {
        authViewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> showLoading(true)
                is AuthState.Success -> {
                    showLoading(false)
                    navigateToMainActivity()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Login Failed: ${state.message}")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_ONE_TAP) {
            try {
                val credential = oneTapClient.getSignInCredentialFromIntent(data)
                val idToken = credential.googleIdToken
                if (idToken != null) {
                    authViewModel.loginWithGoogle(idToken)
                } else {
                    Log.e(TAG, "No Google ID Token!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-in Failed", e)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
