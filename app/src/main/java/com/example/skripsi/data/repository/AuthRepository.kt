package com.example.skripsi.repository

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerWithEmail(email: String, password: String, callback: (AuthResult?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(task.result)
                } else {
                    val exception = task.exception
                    Log.e("AuthRepository", "Registration failed", exception)
                    callback(null)  // You can handle this differently based on the exception
                }
            }
    }

    fun loginWithEmail(email: String, password: String, callback: (AuthResult?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(task.result)
                } else {
                    val exception = task.exception
                    Log.e("AuthRepository", "Login failed", exception)
                    callback(null)
                }
            }
    }

    fun loginWithGoogle(idToken: String, callback: (FirebaseUser?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(auth.currentUser)
                } else {
                    val exception = task.exception
                    Log.e("AuthRepository", "Google login failed", exception)
                    callback(null)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signOut() {
        auth.signOut()
    }
}
