package com.example.nass.ui.common

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nass.data.model.MessageResponse
import com.example.nass.data.model.ProfileUser
import com.example.nass.data.remote.RetrofitClient
import com.example.nass.data.repository.ProfileRepository
import com.example.nass.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =============================================================
// ViewModel
// =============================================================

class SettingsViewModel(private val repo: ProfileRepository) : ViewModel() {

    private val _profile = MutableStateFlow<Resource<ProfileUser>>(Resource.Idle)
    val profile: StateFlow<Resource<ProfileUser>> = _profile.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = Resource.Loading
            _profile.value = repo.getProfile()
        }
    }

    fun saveDisplayName(displayName: String?) {
        viewModelScope.launch {
            _busy.value = true
            when (val result = repo.updateDisplayName(displayName)) {
                is Resource.Success -> {
                    _profile.value = Resource.Success(result.data)
                    _actionMessage.value = "Display name updated"
                }
                is Resource.Error -> _actionMessage.value = result.message
                else -> Unit
            }
            _busy.value = false
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            _busy.value = true
            when (val result = repo.updatePassword(current, new)) {
                is Resource.Success -> _actionMessage.value = result.data.message ?: "Password updated"
                is Resource.Error -> _actionMessage.value = result.message
                else -> Unit
            }
            _busy.value = false
        }
    }

    fun clearMessage() { _actionMessage.value = null }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val api = RetrofitClient.getInstance(context)
                SettingsViewModel(ProfileRepository(api))
            }
        }
    }
}

// =============================================================
// Screen
// =============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(context))

    val profileState by vm.profile.collectAsState()
    val busy by vm.busy.collectAsState()
    val message by vm.actionMessage.collectAsState()

    var displayName by remember { mutableStateOf("") }
    var showPasswordDialog by remember { mutableStateOf(false) }

    // Sync displayName field whenever profile loads
    LaunchedEffect(profileState) {
        val user = (profileState as? Resource.Success)?.data
        if (user != null) displayName = user.displayName ?: ""
    }

    LaunchedEffect(message) {
        message?.let {
            onMessage(it)
            vm.clearMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val user = (profileState as? Resource.Success)?.data

        if (profileState is Resource.Loading || profileState is Resource.Idle) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Column
        }

        if (profileState is Resource.Error) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        (profileState as Resource.Error).message,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { vm.loadProfile() }) { Text("Retry") }
                }
            }
            return@Column
        }

        if (user == null) return@Column

        // ---- Avatar ----
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username.take(1).uppercase(),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "@${user.username}",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = user.email,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = user.role,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // ---- Display Name ----
        Text("Display name", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = displayName,
            onValueChange = { if (it.length <= 100) displayName = it },
            placeholder = { Text("Not set") },
            singleLine = true,
            enabled = !busy,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { vm.saveDisplayName(displayName.ifBlank { null }) },
            enabled = !busy && displayName != (user.displayName ?: ""),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save Display Name")
        }

        Spacer(Modifier.height(28.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))

        // ---- Change Password ----
        OutlinedButton(
            onClick = { showPasswordDialog = true },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Lock, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Change Password")
        }

        Spacer(Modifier.height(12.dp))

        // ---- Logout ----
        OutlinedButton(
            onClick = onLogout,
            enabled = !busy,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Logout")
        }

        Spacer(Modifier.height(24.dp))
    }

    // ---- Change Password dialog ----
    if (showPasswordDialog) {
        var current by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirm by remember { mutableStateOf("") }
        var localError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { if (!busy) showPasswordDialog = false },
            title = { Text("Change password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = current,
                        onValueChange = { current = it; localError = null },
                        label = { Text("Current password") },
                        singleLine = true,
                        enabled = !busy,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; localError = null },
                        label = { Text("New password") },
                        singleLine = true,
                        enabled = !busy,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirm,
                        onValueChange = { confirm = it; localError = null },
                        label = { Text("Confirm new password") },
                        singleLine = true,
                        enabled = !busy,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (localError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(localError!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            current.isBlank() || newPass.isBlank() ->
                                localError = "All fields required"
                            newPass.length < 6 ->
                                localError = "New password must be at least 6 characters"
                            newPass != confirm ->
                                localError = "Passwords don't match"
                            else -> {
                                vm.changePassword(current, newPass)
                                showPasswordDialog = false
                            }
                        }
                    },
                    enabled = !busy
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPasswordDialog = false },
                    enabled = !busy
                ) { Text("Cancel") }
            }
        )
    }
}