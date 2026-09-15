package com.example.nass.ui.seller

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nass.ui.seller.tabs.ListingsTab
import com.example.nass.ui.seller.tabs.StatsTab

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
    val vm: SellerViewModel = viewModel(factory = SellerViewModel.factory(context))
    var currentTab by remember { mutableStateOf(SellerTab.STATS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller — ${currentTab.label}") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
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
                                    SellerTab.LISTINGS -> Icons.AutoMirrored.Filled.List
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
                .padding(padding)
        ) {
            when (currentTab) {
                SellerTab.STATS -> StatsTab(vm)
                SellerTab.LISTINGS -> ListingsTab(vm)
                SellerTab.ADD -> PlaceholderTab("Add Product will be built in Phase C(c)")
                SellerTab.PROFILE -> PlaceholderTab("Profile will be built near the end")
            }
        }
    }
}

@Composable
private fun PlaceholderTab(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

/*package com.example.nass.ui.seller

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
import com.example.nass.ui.seller.tabs.StatsTab

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
    val vm: SellerViewModel = viewModel(factory = SellerViewModel.factory(context))
    var currentTab by remember { mutableStateOf(SellerTab.STATS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller — ${currentTab.label}") },
                actions = {
                    IconButton(onClick = onLogout) {
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
                .padding(padding)
        ) {
            when (currentTab) {
                SellerTab.STATS -> StatsTab(vm)
                SellerTab.LISTINGS -> PlaceholderTab("Listings will be built in Phase C(b)")
                SellerTab.ADD -> PlaceholderTab("Add Product will be built in Phase C(c)")
                SellerTab.PROFILE -> PlaceholderTab("Profile will be built near the end")
            }
        }
    }
}

@Composable
private fun PlaceholderTab(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}


 */