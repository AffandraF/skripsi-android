package com.example.skripsi.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.skripsi.R
import com.example.skripsi.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth

sealed class AuthState {
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String?) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> get() = _authState
    private val firebaseAuth: FirebaseAuth =  FirebaseAuth.getInstance()

    // Register with email and password
    fun registerWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        authRepository.registerWithEmail(email, password) { result ->
            if (result != null) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Registration failed")
            }
        }
    }

    // Login with email and password
    fun loginWithEmail(email: String, password: String) {
        _authState.value = AuthState.Loading
        authRepository.loginWithEmail(email, password) { result ->
            if (result != null) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Login failed")
            }
        }
    }

    fun getGoogleSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getApplication<Application>().getString(R.string.web_client_id))
            .requestEmail()
            .build()
    }

    // Sign in or register with Google
    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success
                    Log.d("AuthViewModel", "Google sign-in successful.")
                } else {
                    _authState.value = AuthState.Error("Google sign-in failed: ${task.exception?.message}")
                    Log.e("AuthViewModel", "Google sign-in failed.", task.exception)
                }
            }
    }

    // Get the current user
    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    // Sign out the user
    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthState.Success  // You can change this to a logged-out state if necessary
    }
}
