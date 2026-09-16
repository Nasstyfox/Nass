package com.example.nass.ui.buyer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nass.ui.buyer.tabs.BrowseTab
import com.example.nass.ui.buyer.tabs.CartTab
import com.example.nass.ui.buyer.tabs.HistoryTab
import com.example.nass.util.Resource
import kotlinx.coroutines.launch

private enum class BuyerTab(val label: String) {
    BROWSE("Browse"),
    CART("Cart"),
    HISTORY("History"),
    PROFILE("Profile")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerDashboardScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val vm: BuyerViewModel = viewModel(factory = BuyerViewModel.factory(context))
    var currentTab by remember { mutableStateOf(BuyerTab.BROWSE) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val selected by vm.selectedProduct.collectAsStateWithLifecycle()
    val cartState by vm.cart.collectAsStateWithLifecycle()
    val cartCount = (cartState as? Resource.Success)?.data?.size ?: 0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Buyer — ${currentTab.label}") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                BuyerTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            if (tab == BuyerTab.CART && cartCount > 0) {
                                BadgedBox(
                                    badge = { Badge { Text(cartCount.toString()) } }
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = tab.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = when (tab) {
                                        BuyerTab.BROWSE -> Icons.Default.Storefront
                                        BuyerTab.CART -> Icons.Default.ShoppingCart
                                        BuyerTab.HISTORY -> Icons.Default.Receipt
                                        BuyerTab.PROFILE -> Icons.Default.Person
                                    },
                                    contentDescription = tab.label
                                )
                            }
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
                BuyerTab.BROWSE -> BrowseTab(
                    vm = vm,
                    onProductClick = { product -> vm.selectProduct(product) }
                )
                BuyerTab.CART -> CartTab(
                    vm = vm,
                    onGoToBrowse = { currentTab = BuyerTab.BROWSE },
                    onMessage = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
                BuyerTab.HISTORY -> HistoryTab(
                    vm = vm,
                    onGoToBrowse = { currentTab = BuyerTab.BROWSE }
                )
                BuyerTab.PROFILE -> PlaceholderTab("Profile will be built near the end")
            }
        }
    }

    selected?.let { product ->
        ProductDetailSheet(
            product = product,
            vm = vm,
            onDismiss = { vm.clearSelectedProduct() },
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