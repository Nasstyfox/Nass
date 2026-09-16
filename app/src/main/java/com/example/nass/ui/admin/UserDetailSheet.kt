package com.example.nass.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nass.data.model.AdminUser

private val ROLES = listOf("buyer", "seller", "admin")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailSheet(
    user: AdminUser,
    vm: AdminViewModel,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit
) {
    val actionState by vm.actionState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var roleExpanded by remember { mutableStateOf(false) }
    var selectedRole by remember(user.id) { mutableStateOf(user.role) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }

    val isBusy = actionState is Resource_Loading

    // React to any action result
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is Resource_Success -> {
                onMessage(s.data)
                vm.clearActionState()
                // Delete closes the sheet; other actions keep it open (list refreshes behind)
                if (s.data.contains("deleted", ignoreCase = true)) {
                    onDismiss()
                }
            }
            is Resource_Error -> {
                onMessage(s.message)
                vm.clearActionState()
            }
            else -> Unit
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isBusy) onDismiss() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        user.displayName ?: user.username,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "@${user.username} · ${user.email}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { if (!isBusy) onDismiss() }, enabled = !isBusy) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(8.dp))

            // Status chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Status:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                Text(user.status.uppercase(), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))

            // ---- Verify button (only for pending) ----
            if (user.status == "pending") {
                Button(
                    onClick = { vm.verifyUser(user.id) },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Verify User")
                }
                Spacer(Modifier.height(16.dp))
            }

            // ---- Role dropdown ----
            Text("Role", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { if (!isBusy) roleExpanded = !roleExpanded }
            ) {
                OutlinedTextField(
                    value = selectedRole,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                    enabled = !isBusy,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    ROLES.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = {
                                selectedRole = r
                                roleExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { vm.updateUserRole(user.id, selectedRole) },
                enabled = !isBusy && selectedRole != user.role,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Role")
            }

            Spacer(Modifier.height(20.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            // ---- Reset password ----
            OutlinedButton(
                onClick = { showResetDialog = true },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reset Password")
            }

            Spacer(Modifier.height(8.dp))

            // ---- Delete ----
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                enabled = !isBusy,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete User")
            }
        }
    }

    // ---- Delete confirm ----
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) showDeleteDialog = false },
            title = { Text("Delete user?") },
            text = {
                Text("\"${user.username}\" and all their listings, cart items, and transactions will be permanently deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = { vm.deleteUser(user.id) },
                    enabled = !isBusy,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !isBusy) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---- Reset password dialog ----
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) { showResetDialog = false; newPassword = "" } },
            title = { Text("Reset password") },
            text = {
                Column {
                    Text("Set a new password for @${user.username}:")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New password") },
                        singleLine = true,
                        enabled = !isBusy,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Minimum 6 characters.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.resetPassword(user.id, newPassword)
                        showResetDialog = false
                        newPassword = ""
                    },
                    enabled = !isBusy && newPassword.length >= 6
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false; newPassword = "" },
                    enabled = !isBusy
                ) { Text("Cancel") }
            }
        )
    }
}

// Local aliases so this file doesn't need to import com.example.nass.util.Resource explicitly
private typealias Resource_Loading = com.example.nass.util.Resource.Loading
private typealias Resource_Success = com.example.nass.util.Resource.Success<String>
private typealias Resource_Error = com.example.nass.util.Resource.Error