package com.example.nass.ui.buyer.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nass.data.model.TransactionItem
import com.example.nass.data.model.TransactionSummary
import com.example.nass.ui.buyer.BuyerViewModel
import com.example.nass.util.Formatters
import com.example.nass.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun HistoryTab(
    vm: BuyerViewModel,
    onGoToBrowse: () -> Unit
) {
    val state by vm.history.collectAsStateWithLifecycle()

    // Load once on first composition
    LaunchedEffect(Unit) {
        if (state is Resource.Idle) vm.loadHistory()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ---- Header ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Purchase History", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when (val s = state) {
                        is Resource.Success ->
                            Formatters.plural(s.data.size, "purchase")
                        else -> "Loading…"
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { vm.loadHistory() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---- Body ----
        when (val s = state) {
            is Resource.Idle, is Resource.Loading -> LoadingBlock()
            is Resource.Error -> ErrorBlock(s.message) { vm.loadHistory() }
            is Resource.Success -> {
                if (s.data.isEmpty()) {
                    EmptyState(onGoToBrowse = onGoToBrowse)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(s.data, key = { it.transactionId }) { tx ->
                            TransactionCard(tx)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================
// Components
// =============================================================

@Composable
private fun TransactionCard(tx: TransactionSummary) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // ---- Header row ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Order #${tx.transactionId}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = formatDate(tx.createdAt) +
                                " · " + Formatters.plural(tx.items.size, "item"),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.price(tx.totalAmount),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ---- Expandable item list ----
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    tx.items.forEachIndexed { index, item ->
                        TransactionItemRow(item)
                        if (index < tx.items.lastIndex) Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItemRow(item: TransactionItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.category,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            Formatters.price(item.priceAtPurchase),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun LoadingBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Loading your purchases…", fontSize = 13.sp)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Couldn't load history",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(onGoToBrowse: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Text("No purchases yet", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "Your orders will appear here",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onGoToBrowse) {
                Text("Browse Listings")
            }
        }
    }
}

// =============================================================
// Helpers
// =============================================================

/**
 * Converts the backend's ISO-8601 timestamp into "15 Sep 2026".
 * Falls back to the raw string if parsing fails.
 */
private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return try {
        // Backend sends "2026-09-15T10:37:00.000Z" style
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date: Date = parser.parse(raw) ?: return raw
        val display = SimpleDateFormat("d MMM yyyy", Locale.US)
        display.format(date)
    } catch (_: Exception) {
        // Try without milliseconds
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date: Date = parser.parse(raw) ?: return raw
            SimpleDateFormat("d MMM yyyy", Locale.US).format(date)
        } catch (_: Exception) {
            raw
        }
    }
}