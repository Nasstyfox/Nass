package com.example.nass.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nass.R
import com.example.nass.data.local.SessionStore
import com.example.nass.ui.admin.tabs.UsersTab
import com.example.nass.ui.common.SettingsScreen
import kotlinx.coroutines.launch

private enum class AdminTab(val labelRes: Int) {
    USERS(R.string.tab_users),
    PROFILE(R.string.tab_profile)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val vm: AdminViewModel = viewModel(factory = AdminViewModel.factory(context))
    var currentTab by remember { mutableStateOf(AdminTab.USERS) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val selected by vm.selectedUser.collectAsStateWithLifecycle()
    val username by SessionStore.from(context).usernameFlow.collectAsState(initial = "")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.welcome_user, username ?: "")) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AdminTab.USERS -> Icons.Default.Group
                                    AdminTab.PROFILE -> Icons.Default.Person
                                },
                                contentDescription = stringResource(tab.labelRes)
                            )
                        },
                        label = { Text(stringResource(tab.labelRes)) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentTab) {
                AdminTab.USERS -> UsersTab(vm = vm, onUserClick = { vm.selectUser(it) })
                AdminTab.PROFILE -> SettingsScreen(
                    onLogout = onLogout,
                    onMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
            }
        }
    }

    selected?.let { user ->
        UserDetailSheet(
            user = user,
            vm = vm,
            onDismiss = { vm.clearSelectedUser() },
            onMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
        )
    }
}