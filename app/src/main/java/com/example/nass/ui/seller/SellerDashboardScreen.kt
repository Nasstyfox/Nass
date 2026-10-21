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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nass.R
import com.example.nass.data.local.SessionStore
import com.example.nass.ui.seller.tabs.AddProductTab
import com.example.nass.ui.seller.tabs.EditProductSheet
import com.example.nass.ui.seller.tabs.ListingsTab
import com.example.nass.ui.seller.tabs.StatsTab
import kotlinx.coroutines.launch
import com.example.nass.ui.common.SettingsScreen

/**
 * Seller dashboard shell.
 */
private enum class SellerTab(val labelRes: Int) {
    STATS(R.string.tab_stats),
    LISTINGS(R.string.tab_listings),
    ADD(R.string.tab_add),
    PROFILE(R.string.tab_profile)
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
                SellerTab.STATS -> StatsTab(vm)
                SellerTab.LISTINGS -> ListingsTab(
                    vm = vm,
                    onCardClick = { product ->
                        if (product.isSold == 1) {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.sold_item_error))
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
                SellerTab.PROFILE -> SettingsScreen(
                    onLogout = onLogout,
                    onMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
            }
        }
    }

    // ---- Edit sheet ----
    editing?.let { product ->
        EditProductSheet(
            vm = vm,
            product = product,
            onDismiss = { vm.cancelEdit() },
            onMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
        )
    }
}