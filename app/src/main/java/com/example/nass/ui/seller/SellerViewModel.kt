package com.example.nass.ui.seller

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nass.data.model.CreateProductRequest
import com.example.nass.data.model.Product
import com.example.nass.data.model.SellerStats
import com.example.nass.data.remote.RetrofitClient
import com.example.nass.data.repository.ProductRepository
import com.example.nass.data.repository.SellerRepository
import com.example.nass.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SellerViewModel(
    private val sellerRepo: SellerRepository,
    private val productRepo: ProductRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<Resource<SellerStats>>(Resource.Idle)
    val stats: StateFlow<Resource<SellerStats>> = _stats.asStateFlow()

    private val _products = MutableStateFlow<Resource<List<Product>>>(Resource.Idle)
    val products: StateFlow<Resource<List<Product>>> = _products.asStateFlow()

    private val _createState = MutableStateFlow<Resource<Product>>(Resource.Idle)
    val createState: StateFlow<Resource<Product>> = _createState.asStateFlow()

    // ---- edit/delete state ----
    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    private val _updateState = MutableStateFlow<Resource<Product>>(Resource.Idle)
    val updateState: StateFlow<Resource<Product>> = _updateState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val deleteState: StateFlow<Resource<Unit>> = _deleteState.asStateFlow()

    init {
        loadStats()
    }

    // ================= Stats / Listings =================

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = Resource.Loading
            _stats.value = sellerRepo.getStats()
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _products.value = Resource.Loading
            _products.value = sellerRepo.getMyProducts()
        }
    }

    // ================= Create =================

    fun createProduct(req: CreateProductRequest) {
        viewModelScope.launch {
            _createState.value = Resource.Loading
            val result = productRepo.create(req)
            _createState.value = result
            if (result is Resource.Success) {
                loadStats()
                loadProducts()
            }
        }
    }

    fun clearCreateState() { _createState.value = Resource.Idle }

    // ================= Edit =================

    fun startEdit(product: Product) { _editingProduct.value = product }

    fun cancelEdit() {
        _editingProduct.value = null
        _updateState.value = Resource.Idle
        _deleteState.value = Resource.Idle
    }

    fun updateProduct(id: Int, req: CreateProductRequest) {
        viewModelScope.launch {
            _updateState.value = Resource.Loading
            val result = productRepo.update(id, req)
            _updateState.value = result
            if (result is Resource.Success) {
                loadStats()
                loadProducts()
            }
        }
    }

    fun clearUpdateState() { _updateState.value = Resource.Idle }

    // ================= Delete =================

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading
            val result = productRepo.delete(id)
            _deleteState.value = result
            if (result is Resource.Success) {
                loadStats()
                loadProducts()
            }
        }
    }

    fun clearDeleteState() { _deleteState.value = Resource.Idle }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val api = RetrofitClient.getInstance(context)
                SellerViewModel(
                    sellerRepo = SellerRepository(api),
                    productRepo = ProductRepository(api)
                )
            }
        }
    }
}