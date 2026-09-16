package com.example.nass.ui.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nass.data.model.AdminUser
import com.example.nass.data.model.ResetPasswordRequest
import com.example.nass.data.model.UpdateUserRequest
import com.example.nass.data.remote.RetrofitClient
import com.example.nass.data.repository.AdminRepository
import com.example.nass.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repo: AdminRepository) : ViewModel() {

    private val _users = MutableStateFlow<Resource<List<AdminUser>>>(Resource.Idle)
    val users: StateFlow<Resource<List<AdminUser>>> = _users.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val actionState: StateFlow<Resource<String>> = _actionState.asStateFlow()

    private val _selectedUser = MutableStateFlow<AdminUser?>(null)
    val selectedUser: StateFlow<AdminUser?> = _selectedUser.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = Resource.Loading
            _users.value = repo.getAllUsers()
        }
    }

    fun verifyUser(id: Int) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = repo.verifyUser(id)
            _actionState.value = when (result) {
                is Resource.Success -> Resource.Success(result.data.message ?: "User verified")
                is Resource.Error -> Resource.Error(result.message, result.code)
                else -> Resource.Idle
            }
            if (result is Resource.Success) reload()
        }
    }

    fun updateUserRole(id: Int, newRole: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = repo.updateUser(id, UpdateUserRequest(role = newRole))
            _actionState.value = when (result) {
                is Resource.Success -> Resource.Success("Role updated to $newRole")
                is Resource.Error -> Resource.Error(result.message, result.code)
                else -> Resource.Idle
            }
            if (result is Resource.Success) reload()
        }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = repo.deleteUser(id)
            _actionState.value = when (result) {
                is Resource.Success -> {
                    _selectedUser.value = null
                    Resource.Success("User deleted")
                }
                is Resource.Error -> Resource.Error(result.message, result.code)
                else -> Resource.Idle
            }
            if (result is Resource.Success) reload()
        }
    }

    fun resetPassword(id: Int, newPassword: String) {
        viewModelScope.launch {
            _actionState.value = Resource.Loading
            val result = repo.resetPassword(id, newPassword)
            _actionState.value = when (result) {
                is Resource.Success -> Resource.Success(result.data.message ?: "Password reset")
                is Resource.Error -> Resource.Error(result.message, result.code)
                else -> Resource.Idle
            }
        }
    }

    fun selectUser(user: AdminUser) { _selectedUser.value = user }
    fun clearSelectedUser() { _selectedUser.value = null }
    fun clearActionState() { _actionState.value = Resource.Idle }

    private fun reload() {
        loadUsers()
    }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val api = RetrofitClient.getInstance(context)
                AdminViewModel(AdminRepository(api))
            }
        }
    }
}