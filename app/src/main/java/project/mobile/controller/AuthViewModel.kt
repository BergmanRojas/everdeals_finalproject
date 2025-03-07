package project.mobile.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import project.mobile.tools.SessionStorage
import project.mobile.models.AuthState

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val firebaseAuth = FirebaseAuth.getInstance()

    init {
        checkAuthState()
    }

    // Verifica el estado de autenticación al iniciar
    private fun checkAuthState() {
        viewModelScope.launch {
            try {
                val currentUser = firebaseAuth.currentUser
                val isLoggedIn = sessionStorage.isLoggedIn.first()
                if (currentUser != null && isLoggedIn) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to check auth state: ${e.message}")
            }
        }
    }

    // Inicia sesión con email y contraseña
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = userRepository.signIn(email, password)
                if (result) {
                    sessionStorage.setLoggedIn(true)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Invalid credentials")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign-in failed: ${e.message}")
            }
        }
    }

    // Registra un nuevo usuario
    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = userRepository.signUp(email, password, username)
                if (result) {
                    sessionStorage.setLoggedIn(true)
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    // Cierra la sesión del usuario
    fun signOut() {
        viewModelScope.launch {
            try {
                userRepository.signOut()
                sessionStorage.setLoggedIn(false)
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Sign-out failed: ${e.message}")
            }
        }
    }
}