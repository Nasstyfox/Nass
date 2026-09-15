package com.example.nass.ui.seller.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nass.data.model.CreateProductRequest
import com.example.nass.data.model.Product
import com.example.nass.ui.seller.SellerViewModel
import com.example.nass.util.Resource

private val CATEGORIES = listOf("Clothing", "Shoes", "Accessories", "Bags", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductSheet(
    vm: SellerViewModel,
    product: Product,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit
) {
    val updateState by vm.updateState.collectAsStateWithLifecycle()
    val deleteState by vm.deleteState.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Pre-fill from product — keyed on id so switching products refreshes fields
    var name by remember(product.id) { mutableStateOf(product.name) }
    var description by remember(product.id) { mutableStateOf(product.description ?: "") }
    var priceText by remember(product.id) {
        mutableStateOf(
            if (product.price == 0.0) "" else String.format(java.util.Locale.US, "%.2f", product.price)
        )
    }
    var category by remember(product.id) { mutableStateOf(product.category) }
    var imageUrl by remember(product.id) { mutableStateOf(product.imageUrl ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val priceValue = priceText.toDoubleOrNull()
    val isFormValid = name.isNotBlank() && priceValue != null && priceValue > 0
    val isUpdating = updateState is Resource.Loading
    val isDeleting = deleteState is Resource.Loading
    val isBusy = isUpdating || isDeleting

    // ---- React to update result ----
    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is Resource.Success -> {
                onMessage("Listing updated")
                vm.cancelEdit()      // closes sheet, clears states
            }
            is Resource.Error -> {
                onMessage(s.message)
                vm.clearUpdateState()
            }
            else -> Unit
        }
    }

    // ---- React to delete result ----
    LaunchedEffect(deleteState) {
        when (val s = deleteState) {
            is Resource.Success -> {
                showDeleteDialog = false
                onMessage("Listing deleted")
                vm.cancelEdit()
            }
            is Resource.Error -> {
                onMessage(s.message)
                vm.clearDeleteState()
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
            // ---- Header ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Edit listing", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Update the details below",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { if (!isBusy) onDismiss() },
                    enabled = !isBusy
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---- Image preview ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item name *") },
                singleLine = true,
                enabled = !isBusy,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 3,
                maxLines = 5,
                enabled = !isBusy,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { input ->
                    if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        priceText = input
                    }
                },
                label = { Text("Price (R) *") },
                singleLine = true,
                enabled = !isBusy,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                isError = priceText.isNotEmpty() && priceValue == null,
                supportingText = {
                    if (priceText.isNotEmpty() && priceValue == null)
                        Text("Enter a valid number")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { if (!isBusy) categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    CATEGORIES.forEach { c ->
                        DropdownMenuItem(
                            text = { Text(c) },
                            onClick = { category = c; categoryExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL (optional)") },
                singleLine = true,
                enabled = !isBusy,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ---- Update button ----
            Button(
                onClick = {
                    val req = CreateProductRequest(
                        name = name.trim(),
                        description = description.trim().ifBlank { null },
                        price = priceValue!!,
                        category = category,
                        imageUrl = imageUrl.trim().ifBlank { null }
                    )
                    vm.updateProduct(product.id, req)
                },
                enabled = isFormValid && !isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }

            Spacer(Modifier.height(8.dp))

            // ---- Delete button ----
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                enabled = !isBusy,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Delete Listing")
            }
        }
    }

    // ---- Delete confirmation dialog ----
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Delete listing?") },
            text = { Text("\"${product.name}\" will be permanently removed. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { vm.deleteProduct(product.id) },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Delete")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) { Text("Cancel") }
            }
        )
    }
}