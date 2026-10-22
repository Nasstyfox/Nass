package com.example.nass.ui.buyer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nass.data.local.NassDatabase
import com.example.nass.data.local.SessionStore
import com.example.nass.data.model.AddToCartResponse
import com.example.nass.data.model.CartItem
import com.example.nass.data.model.CheckoutResponse
import com.example.nass.data.model.Product
import com.example.nass.data.model.TransactionSummary
import com.example.nass.data.remote.RetrofitClient
import com.example.nass.data.repository.CartRepository
import com.example.nass.data.repository.FirestoreRepository
import com.example.nass.data.repository.ProductRepository
import com.example.nass.util.Logger
import com.example.nass.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BuyerViewModel(
    private val productRepo: ProductRepository,
    private val cartRepo: CartRepository,
    private val firestoreRepo: FirestoreRepository
) : ViewModel() {

    private val _available = MutableStateFlow<Resource<List<Product>>>(Resource.Idle)
    val available: StateFlow<Resource<List<Product>>> = _available.asStateFlow()

    private val _cart = MutableStateFlow<Resource<List<CartItem>>>(Resource.Idle)
    val cart: StateFlow<Resource<List<CartItem>>> = _cart.asStateFlow()

    private val _addToCartState = MutableStateFlow<Resource<AddToCartResponse>>(Resource.Idle)
    val addToCartState: StateFlow<Resource<AddToCartResponse>> = _addToCartState.asStateFlow()

    private val _removeFromCartState = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val removeFromCartState: StateFlow<Resource<Unit>> = _removeFromCartState.asStateFlow()

    private val _checkoutState = MutableStateFlow<Resource<CheckoutResponse>>(Resource.Idle)
    val checkoutState: StateFlow<Resource<CheckoutResponse>> = _checkoutState.asStateFlow()

    private val _history = MutableStateFlow<Resource<List<TransactionSummary>>>(Resource.Idle)
    val history: StateFlow<Resource<List<TransactionSummary>>> = _history.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _favorites = MutableStateFlow<Set<Int>>(emptySet())
    val favorites: StateFlow<Set<Int>> = _favorites.asStateFlow()

    init {
        loadAvailable()
        loadCart()
    }

    fun loadAvailable() {
        viewModelScope.launch {
            _available.value = Resource.Loading
            _available.value = productRepo.getAvailable()
        }
    }

    fun loadFavorites(context: Context) {
        viewModelScope.launch {
            val userId = SessionStore.from(context).currentUserId()?.toString() ?: return@launch
            when (val result = firestoreRepo.getFavorites(userId)) {
                is Resource.Success -> _favorites.value = result.data.toSet()
                is Resource.Error -> Logger.e("BuyerVM", result.message)
                else -> Unit
            }
        }
    }

    fun toggleFavorite(context: Context, productId: Int) {
        viewModelScope.launch {
            val userId = SessionStore.from(context).currentUserId()?.toString() ?: return@launch
            val isCurrentlyFav = _favorites.value.contains(productId)

            _favorites.value = if (isCurrentlyFav) {
                _favorites.value - productId
            } else {
                _favorites.value + productId
            }

            if (isCurrentlyFav) {
                firestoreRepo.removeFavorite(userId, productId)
            } else {
                firestoreRepo.addFavorite(userId, productId)
            }
        }
    }

    fun loadCart() {
        viewModelScope.launch {
            _cart.value = Resource.Loading
            _cart.value = cartRepo.getCart()
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch {
            _addToCartState.value = Resource.Loading
            val result = cartRepo.addToCart(productId)
            _addToCartState.value = result
            if (result is Resource.Success) loadCart()
        }
    }

    fun clearAddToCartState() { _addToCartState.value = Resource.Idle }

    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            _removeFromCartState.value = Resource.Loading
            val result = cartRepo.removeFromCart(cartItemId)
            _removeFromCartState.value = result
            if (result is Resource.Success) loadCart()
        }
    }

    fun clearRemoveFromCartState() { _removeFromCartState.value = Resource.Idle }

    fun checkout() {
        viewModelScope.launch {
            _checkoutState.value = Resource.Loading
            val result = cartRepo.checkout()
            _checkoutState.value = result
            if (result is Resource.Success) {
                loadCart()
                loadAvailable()
                loadHistory()
            }
        }
    }

    fun clearCheckoutState() { _checkoutState.value = Resource.Idle }

    fun loadHistory() {
        viewModelScope.launch {
            _history.value = Resource.Loading
            _history.value = cartRepo.getHistory()
        }
    }

    fun selectProduct(product: Product) { _selectedProduct.value = product }
    fun clearSelectedProduct() { _selectedProduct.value = null }

    companion object {
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val api = RetrofitClient.getInstance(context)
                val dao = NassDatabase.getDatabase(context).nassDao() // <-- DB Initialized
                BuyerViewModel(
                    productRepo = ProductRepository(api, dao),
                    cartRepo = CartRepository(api, dao),
                    firestoreRepo = FirestoreRepository()
                )
            }
        }
    }
}