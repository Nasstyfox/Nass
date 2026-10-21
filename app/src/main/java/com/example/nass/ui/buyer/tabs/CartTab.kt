package com.example.nass.ui.buyer.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nass.R
import com.example.nass.data.model.CartItem
import com.example.nass.ui.buyer.BuyerViewModel
import com.example.nass.util.Formatters
import com.example.nass.util.Resource

@Composable
fun CartTab(
    vm: BuyerViewModel,
    onGoToBrowse: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val state by vm.cart.collectAsStateWithLifecycle()
    val checkoutState by vm.checkoutState.collectAsStateWithLifecycle()
    val removeState by vm.removeFromCartState.collectAsStateWithLifecycle()

    var showCheckoutDialog by remember { mutableStateOf(false) }
    val isCheckingOut = checkoutState is Resource.Loading

    LaunchedEffect(checkoutState) {
        when (val s = checkoutState) {
            is Resource.Success -> {
                showCheckoutDialog = false
                onMessage(context.getString(R.string.cart_purchase_complete, Formatters.price(s.data.totalAmount)))
                vm.clearCheckoutState()
            }
            is Resource.Error -> {
                showCheckoutDialog = false
                onMessage(s.message)
                vm.clearCheckoutState()
            }
            else -> Unit
        }
    }

    LaunchedEffect(removeState) {
        when (val s = removeState) {
            is Resource.Success -> {
                onMessage(context.getString(R.string.cart_removed))
                vm.clearRemoveFromCartState()
            }
            is Resource.Error -> {
                onMessage(s.message)
                vm.clearRemoveFromCartState()
            }
            else -> Unit
        }
    }

    val cartItems = (state as? Resource.Success)?.data.orEmpty()
    val total = cartItems.sumOf { it.price }

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
                Text(stringResource(R.string.buyer_cart_title), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when (val s = state) {
                        is Resource.Success -> if (s.data.size == 1) "1 ${stringResource(R.string.item_singular)}" else "${s.data.size} ${stringResource(R.string.item_plural)}"
                        else -> stringResource(R.string.loading)
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { vm.loadCart() }, enabled = !isCheckingOut) {
                Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.btn_refresh))
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (val s = state) {
                is Resource.Idle, is Resource.Loading -> LoadingBlock()
                is Resource.Error -> ErrorBlock(s.message) { vm.loadCart() }
                is Resource.Success -> {
                    if (s.data.isEmpty()) {
                        EmptyState(onGoToBrowse = onGoToBrowse)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(s.data, key = { it.cartItemId }) { item ->
                                CartRow(
                                    item = item,
                                    enabled = !isCheckingOut,
                                    onRemove = { vm.removeFromCart(item.cartItemId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (cartItems.isNotEmpty()) {
            Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.cart_total),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            Formatters.price(total),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showCheckoutDialog = true },
                        enabled = !isCheckingOut,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (isCheckingOut) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.cart_checkout), fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        AlertDialog(
            onDismissRequest = { if (!isCheckingOut) showCheckoutDialog = false },
            title = { Text(stringResource(R.string.confirm_purchase)) },
            text = {
                Column {
                    val countMsg = if (cartItems.size == 1) "1 ${context.getString(R.string.item_singular)}" else "${cartItems.size} ${context.getString(R.string.item_plural)}"
                    Text(stringResource(R.string.cart_buy_msg, countMsg))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.cart_total_msg, Formatters.price(total)), fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.cart_warning), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { vm.checkout() }, enabled = !isCheckingOut) {
                    if (isCheckingOut) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.btn_confirm))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }, enabled = !isCheckingOut) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun CartRow(item: CartItem, enabled: Boolean, onRemove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(model = item.imageUrl, contentDescription = item.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!item.sellerUsername.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(stringResource(R.string.by_seller, item.sellerUsername), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                Text(Formatters.price(item.price), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            IconButton(onClick = onRemove, enabled = enabled) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.cart_remove_desc), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.cart_loading), fontSize = 13.sp)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.cart_error), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onErrorContainer)
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

@Composable
private fun EmptyState(onGoToBrowse: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.cart_empty_title), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.cart_empty_sub), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onGoToBrowse) {
                Text(stringResource(R.string.btn_browse_listings))
            }
        }
    }
}