package project.mobile.controller

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import project.mobile.model.AuthRepository
import project.mobile.model.UserPreferences
import project.mobile.model.AuthState

class AuthViewModel(
    application: Application,
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            userPreferences.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    val sessionRefreshed = authRepository.refreshSession()
                    if (sessionRefreshed) {
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Idle
                    }
                } else {
                    _authState.value = AuthState.Idle
                }
            }
        }
    }

    fun signUp(email: String, password: String, username: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Starting sign up process")
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signUp(email, password, username)
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "Sign up successful")
                    _authState.value = AuthState.Success
                } else {
                    Log.e("AuthViewModel", "Sign up failed: ${result.exceptionOrNull()?.message}")
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign up exception: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = authRepository.signIn(email, password)
                if (result.isSuccess) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Idle
        }
    }

    class Factory(
        private val application: Application,
        private val authRepository: AuthRepository,
        private val userPreferences: UserPreferences
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(application, authRepository, userPreferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

