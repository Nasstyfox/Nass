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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nass.ui.seller.tabs.AddProductTab
import com.example.nass.ui.seller.tabs.EditProductSheet
import com.example.nass.ui.seller.tabs.ListingsTab
import com.example.nass.ui.seller.tabs.StatsTab
import kotlinx.coroutines.launch

/**
 * Seller dashboard shell.
 *
 * Bottom-nav tabs map to the seller's main workflows:
 *  - Stats     → live counts of added / sold / available
 *  - Listings  → all products with filter chips + tap to edit
 *  - Add       → form for creating a new listing
 *  - Profile   → placeholder, built last
 */
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val editing by vm.editingProduct.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                SellerTab.LISTINGS -> ListingsTab(
                    vm = vm,
                    onCardClick = { product ->
                        if (product.isSold == 1) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Sold items can't be edited")
                            }
                        } else {
                            vm.startEdit(product)
                        }
                    }
                )
                SellerTab.ADD -> AddProductTab(
                    vm = vm,
                    onProductAdded = { currentTab = SellerTab.LISTINGS }
                )
                SellerTab.PROFILE -> PlaceholderTab("Profile will be built near the end")
            }
        }
    }

    // ---- Edit sheet, hoisted so the snackbar survives sheet dismissal ----
    editing?.let { product ->
        EditProductSheet(
            vm = vm,
            product = product,
            onDismiss = { vm.cancelEdit() },
            onMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
        )
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