package com.example.nass.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nass.data.local.TokenManager
import com.example.nass.data.model.RegisterRequest
import com.example.nass.data.remote.RetrofitClient
import com.example.nass.data.repository.AuthRepository
import com.example.nass.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.nass.data.local.SessionManager
data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccessRole: String? = null,
    val registerSuccess: Boolean = false,
    val registerMessage: String? = null
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Please enter username and password")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            when (val result = repo.login(username.trim(), password)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    loginSuccessRole = result.data.user.role
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
                else -> Unit
            }
        }
    }

    fun register(username: String, email: String, password: String, confirmPassword: String, role: String) {
        if (listOf(username, email, password, confirmPassword).any { it.isBlank() }) {
            _state.value = _state.value.copy(errorMessage = "All fields are required")
            return
        }
        if (password != confirmPassword) {
            _state.value = _state.value.copy(errorMessage = "Passwords do not match")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val req = RegisterRequest(username.trim(), email.trim(), password, confirmPassword, role)
            when (val result = repo.register(req)) {
                is Resource.Success -> _state.value = _state.value.copy(
                    isLoading = false,
                    registerSuccess = true,
                    registerMessage = result.data.message
                )
                is Resource.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = result.message
                )
                else -> Unit
            }
        }
    }

    fun consumeLoginSuccess() {
        _state.value = _state.value.copy(loginSuccessRole = null)
    }

    fun consumeRegisterSuccess() {
        _state.value = _state.value.copy(registerSuccess = false, registerMessage = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val api = RetrofitClient.getInstance(context)
                val session = TokenManager(context.applicationContext)
                AuthViewModel(AuthRepository(api = api, session = session))
            }
        }
    }
}