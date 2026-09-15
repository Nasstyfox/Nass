package com.example.nass.ui.seller

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nass.ui.auth.AuthViewModel
import kotlinx.coroutines.launch

private enum class SellerTab(val label: String) {
    STATS("Stats"),
    LISTINGS("Listings"),
    ADD("Add"),
    PROFILE("Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.factory(context))
    var currentTab by remember { mutableStateOf(SellerTab.STATS) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Seller — ${currentTab.label}") },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            // clear the session via repository, then route to login
                            // We'll wire a proper logout in Phase C
                            onLogout()
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                SellerTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    SellerTab.STATS -> Icons.Default.BarChart
                                    SellerTab.LISTINGS -> Icons.Default.List
                                    SellerTab.ADD -> Icons.Default.AddCircle
                                    SellerTab.PROFILE -> Icons.Default.Person
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Phase C will fill in: ${currentTab.label}")
        }
    }
}