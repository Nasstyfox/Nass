package com.example.nass.ui.seller.tabs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nass.R
import com.example.nass.data.model.CreateProductRequest
import com.example.nass.ui.seller.SellerViewModel
import com.example.nass.util.Resource

private val CATEGORIES = listOf("Clothing", "Shoes", "Accessories", "Bags", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductTab(
    vm: SellerViewModel,
    onProductAdded: () -> Unit
) {
    val context = LocalContext.current
    val state by vm.createState.collectAsStateWithLifecycle()
    val uploadState by vm.imageUpload.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Clothing") }
    var imageUrl by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    val priceValue = priceText.toDoubleOrNull()
    val isFormValid = name.isNotBlank() && priceValue != null && priceValue > 0
    val isLoading = state is Resource.Loading
    val isUploading = uploadState is Resource.Loading

    val pickImage = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) vm.uploadProductImage(context, uri)
    }

    LaunchedEffect(uploadState) {
        when (val s = uploadState) {
            is Resource.Success -> { imageUrl = s.data; vm.clearImageUpload() }
            is Resource.Error -> { snackbarHost.showSnackbar(s.message); vm.clearImageUpload() }
            else -> Unit
        }
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is Resource.Success -> {
                snackbarHost.showSnackbar(context.getString(R.string.msg_product_added))
                name = ""; description = ""; priceText = ""; category = "Clothing"; imageUrl = ""
                vm.clearCreateState()
                onProductAdded()
            }
            is Resource.Error -> { snackbarHost.showSnackbar(s.message); vm.clearCreateState() }
            else -> Unit
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHost) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Text(stringResource(R.string.add_title), fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.add_subtitle), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))

            // ---- Image preview ----
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotBlank() && imageUrl.startsWith("http")) {
                    AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    IconButton(
                        onClick = { imageUrl = "" },
                        enabled = !isLoading && !isUploading,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                    ) {
                        Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), shape = RoundedCornerShape(50)) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.padding(6.dp))
                        }
                    }
                } else if (isUploading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.image_uploading), fontSize = 12.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(stringResource(R.string.image_no_image), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = { pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                enabled = !isLoading && !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (imageUrl.isBlank()) R.string.image_add_btn else R.string.image_change_btn))
            }

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.field_item_name)) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.field_description)) },
                minLines = 3,
                maxLines = 5,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { input -> if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) priceText = input },
                label = { Text(stringResource(R.string.field_price)) },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                isError = priceText.isNotEmpty() && priceValue == null,
                supportingText = { if (priceText.isNotEmpty() && priceValue == null) Text(stringResource(R.string.field_price_error)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { if (!isLoading) categoryExpanded = !categoryExpanded }
            ) {
                // Translate the visual category, but keep the English one in state for the DB
                val visualCategory = when(category) {
                    "Clothing" -> stringResource(R.string.cat_clothing)
                    "Shoes" -> stringResource(R.string.cat_shoes)
                    "Accessories" -> stringResource(R.string.cat_accessories)
                    "Bags" -> stringResource(R.string.cat_bags)
                    "Other" -> stringResource(R.string.cat_other)
                    else -> category
                }

                OutlinedTextField(
                    value = visualCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    enabled = !isLoading,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    CATEGORIES.forEach { c ->
                        val menuText = when(c) {
                            "Clothing" -> stringResource(R.string.cat_clothing)
                            "Shoes" -> stringResource(R.string.cat_shoes)
                            "Accessories" -> stringResource(R.string.cat_accessories)
                            "Bags" -> stringResource(R.string.cat_bags)
                            "Other" -> stringResource(R.string.cat_other)
                            else -> c
                        }
                        DropdownMenuItem(
                            text = { Text(menuText) },
                            onClick = { category = c; categoryExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val req = CreateProductRequest(
                        name = name.trim(),
                        description = description.trim().ifBlank { null },
                        price = priceValue!!,
                        category = category,
                        imageUrl = imageUrl.trim().ifBlank { null }
                    )
                    vm.createProduct(req)
                },
                enabled = isFormValid && !isLoading && !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_post_item))
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.field_required), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}