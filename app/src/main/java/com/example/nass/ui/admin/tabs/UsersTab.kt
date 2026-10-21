package com.example.nass.ui.admin.tabs

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nass.R
import com.example.nass.data.model.AdminUser
import com.example.nass.ui.admin.AdminViewModel
import com.example.nass.util.Resource

// Notice how we use @StringRes here instead of hardcoded Strings!
private enum class UserFilter(@StringRes val labelRes: Int) {
    ALL(R.string.filter_all),
    PENDING(R.string.filter_pending),
    ACTIVE(R.string.filter_active),
    SUSPENDED(R.string.filter_suspended)
}

@Composable
fun UsersTab(
    vm: AdminViewModel,
    onUserClick: (AdminUser) -> Unit
) {
    val state by vm.users.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(UserFilter.ALL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                // Translates the title "Users"
                Text(stringResource(R.string.tab_users), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when (val s = state) {
                        is Resource.Success -> if (s.data.size == 1) {
                            stringResource(R.string.admin_users_count_single, s.data.size)
                        } else {
                            stringResource(R.string.admin_users_count, s.data.size)
                        }
                        else -> stringResource(R.string.loading)
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { vm.loadUsers() }) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.btn_refresh))
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state is Resource.Success) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UserFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        // Translates the chips "All, Pending, Active, Suspended"
                        label = { Text(stringResource(f.labelRes)) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        when (val s = state) {
            is Resource.Idle, is Resource.Loading -> LoadingBlock()
            is Resource.Error -> ErrorBlock(s.message) { vm.loadUsers() }
            is Resource.Success -> {
                val filtered = when (filter) {
                    UserFilter.ALL -> s.data
                    UserFilter.PENDING -> s.data.filter { it.status == "pending" }
                    UserFilter.ACTIVE -> s.data.filter { it.status == "active" }
                    UserFilter.SUSPENDED -> s.data.filter { it.status == "suspended" }
                }
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.admin_filter_empty, stringResource(filter.labelRes).lowercase()),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.id }) { user ->
                            UserCard(user, onClick = { onUserClick(user) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCard(user: AdminUser, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = user.displayName ?: user.username,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.displayName != null) {
                        Text("@${user.username}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                StatusBadge(user.status)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = user.email,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RoleBadge(user.role)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.admin_id_label, user.id), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "active" -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
        "pending" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }

    val statusText = when(status) {
        "active" -> stringResource(R.string.status_active)
        "pending" -> stringResource(R.string.status_pending)
        else -> stringResource(R.string.status_suspended)
    }

    Surface(color = bg, shape = MaterialTheme.shapes.small) {
        Text(
            text = statusText,
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun RoleBadge(role: String) {
    val roleText = when(role) {
        "buyer" -> stringResource(R.string.role_buyer)
        "seller" -> stringResource(R.string.role_seller)
        else -> stringResource(R.string.role_admin)
    }

    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
        Text(
            text = roleText,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun LoadingBlock() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.admin_loading_users), fontSize = 13.sp)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.admin_users_error), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.height(4.dp))
            Text(message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.btn_retry))
            }
        }
    }
}