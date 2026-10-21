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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nass.R
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

    val isBusy = actionState is com.example.nass.util.Resource.Loading

    // Hoist strings for LaunchedEffect
    val msgDeleteSuccess = stringResource(R.string.msg_listing_deleted) // Or a specific "User deleted" string if you have one

    // React to any action result
    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is com.example.nass.util.Resource.Success -> {
                onMessage(s.data)
                vm.clearActionState()
                // Delete closes the sheet
                if (s.data.contains("deleted", ignoreCase = true) || s.data.contains("verwyder", ignoreCase = true) || s.data.contains("suswa", ignoreCase = true)) {
                    onDismiss()
                }
            }
            is com.example.nass.util.Resource.Error -> {
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
                    Text(user.displayName ?: user.username, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text("@${user.username} · ${user.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { if (!isBusy) onDismiss() }, enabled = !isBusy) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.btn_cancel))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Status chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.admin_status_label), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
                val statusText = when(user.status) {
                    "active" -> stringResource(R.string.status_active)
                    "pending" -> stringResource(R.string.status_pending)
                    else -> stringResource(R.string.status_suspended)
                }
                Text(statusText, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))

            // Verify button
            if (user.status == "pending") {
                Button(
                    onClick = { vm.verifyUser(user.id) },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.admin_btn_verify))
                }
                Spacer(Modifier.height(16.dp))
            }

            // Role dropdown
            Text(stringResource(R.string.admin_role_label), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = { if (!isBusy) roleExpanded = !roleExpanded }
            ) {
                val visualRole = when(selectedRole) {
                    "buyer" -> stringResource(R.string.role_buyer)
                    "seller" -> stringResource(R.string.role_seller)
                    else -> stringResource(R.string.role_admin)
                }

                OutlinedTextField(
                    value = visualRole,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(roleExpanded) },
                    enabled = !isBusy,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = { roleExpanded = false }
                ) {
                    ROLES.forEach { r ->
                        val menuText = when(r) {
                            "buyer" -> stringResource(R.string.role_buyer)
                            "seller" -> stringResource(R.string.role_seller)
                            else -> stringResource(R.string.role_admin)
                        }
                        DropdownMenuItem(
                            text = { Text(menuText) },
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
                Text(stringResource(R.string.admin_btn_save_role))
            }

            Spacer(Modifier.height(20.dp))

            HorizontalDivider()

            Spacer(Modifier.height(16.dp))

            // Reset password
            OutlinedButton(
                onClick = { showResetDialog = true },
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Lock, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.admin_btn_reset_pwd))
            }

            Spacer(Modifier.height(8.dp))

            // Delete
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                enabled = !isBusy,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.admin_btn_delete_user))
            }
        }
    }

    // Delete confirm
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) showDeleteDialog = false },
            title = { Text(stringResource(R.string.admin_delete_title)) },
            text = { Text(stringResource(R.string.admin_delete_msg, user.username)) },
            confirmButton = {
                TextButton(
                    onClick = { vm.deleteUser(user.id) },
                    enabled = !isBusy,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.btn_delete))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !isBusy) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Reset password dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { if (!isBusy) { showResetDialog = false; newPassword = "" } },
            title = { Text(stringResource(R.string.admin_reset_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.admin_reset_msg, user.username))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(stringResource(R.string.admin_field_new_pwd)) },
                        singleLine = true,
                        enabled = !isBusy,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(stringResource(R.string.admin_reset_hint), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text(stringResource(R.string.admin_btn_reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false; newPassword = "" }, enabled = !isBusy) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}