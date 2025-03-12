package project.mobile.controller

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import project.mobile.model.AuthRepository
import project.mobile.model.UserPreferences
import project.mobile.model.AuthState
import project.mobile.model.User

class AuthManager(
    private val repository: AuthRepository,
    private val context: Context
) {
    private val userPreferences = UserPreferences(context)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        coroutineScope.launch {
            userPreferences.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value = AuthState.Idle
                }
            }
        }
    }

    suspend fun checkSession(): Boolean {
        return try {
            val isLoggedIn = repository.refreshSession()
            if (isLoggedIn) {
                val user = getCurrentUser()
                if (user != null) {
                    _authState.value = AuthState.Success
                    true
                } else {
                    _authState.value = AuthState.Idle
                    false
                }
            } else {
                _authState.value = AuthState.Idle
                false
            }
        } catch (e: Exception) {
            Log.e("AuthManager", "Error checking session", e)
            _authState.value = AuthState.Error(e.message ?: "Failed to check session")
            false
        }
    }

    suspend fun signIn(email: String, password: String) {
        try {
            _authState.value = AuthState.Loading
            val result = repository.signIn(email, password)
            if (result.isSuccess) {
                userPreferences.setLoggedIn(true)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun signUp(email: String, password: String, username: String) {
        try {
            _authState.value = AuthState.Loading
            val result = repository.signUp(email, password, username)
            if (result.isSuccess) {
                userPreferences.setLoggedIn(true)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun signOut() {
        repository.signOut()
        userPreferences.setLoggedIn(false)
        _authState.value = AuthState.Idle
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val user = repository.getCurrentUser()
            if (user == null) {
                _authState.value = AuthState.Idle
            }
            user
        } catch (e: Exception) {
            Log.e("AuthManager", "Error getting current user", e)
            _authState.value = AuthState.Error(e.message ?: "Failed to get current user")
            null
        }
    }
}

