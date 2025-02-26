package project.mobile.controller

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RegisterController {

    private val auth = FirebaseAuth.getInstance() // Firebase Auth instance

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered

    // Registrar un nuevo usuario con Firebase
    fun registerUser(email: String, password: String) {
        _isLoading.value = true
        _error.value = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    // Registro exitoso
                    _isRegistered.value = true
                } else {
                    // Error en el registro
                    _error.value = task.exception?.message ?: "Registration failed"
                    Log.e("RegisterController", "Registration failed: ${task.exception}")
                }
            }
    }
}