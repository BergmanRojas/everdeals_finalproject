package project.mobile.controller

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import project.mobile.model.Product
import project.mobile.model.ProductRepository
import project.mobile.util.AmazonScraper
import project.mobile.model.AuthRepository
import project.mobile.model.User

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository,
    private val amazonScraper: AmazonScraper,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _scrapingState = MutableStateFlow<ScrapingState>(ScrapingState.Idle)
    val scrapingState: StateFlow<ScrapingState> = _scrapingState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    init {
        loadProducts()
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            if (query.isEmpty()) {
                _products.value = _allProducts.value
            } else {
                _products.value = _allProducts.value.filter { product ->
                    product.name.contains(query, ignoreCase = true) ||
                            product.description.contains(query, ignoreCase = true) ||
                            product.category.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                Log.d("ProductViewModel", "Loading products...")
                val loadedProducts = repository.getProducts()
                Log.d("ProductViewModel", "Products loaded: ${loadedProducts.size}")
                _allProducts.value = loadedProducts

                // Apply search filter if there's an active search
                if (_searchQuery.value.isNotEmpty()) {
                    searchProducts(_searchQuery.value)
                } else {
                    _products.value = loadedProducts
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading products", e)
                _products.value = emptyList()
            }
        }
    }

    fun loadProductsByVotes() {
        viewModelScope.launch {
            try {
                val loadedProducts = repository.getProducts()
                val sortedProducts = loadedProducts.sortedByDescending { it.likes - it.dislikes }
                _allProducts.value = sortedProducts

                // Apply search filter if there's an active search
                if (_searchQuery.value.isNotEmpty()) {
                    searchProducts(_searchQuery.value)
                } else {
                    _products.value = sortedProducts
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading products by votes", e)
            }
        }
    }

    fun loadProductsByRising() {
        viewModelScope.launch {
            try {
                val loadedProducts = repository.getProducts()
                // This is a simplified "rising" algorithm - in a real app you might want to
                // calculate a trending score based on recent votes and time
                val sortedProducts = loadedProducts.sortedWith(
                    compareByDescending<Product> { it.likes - it.dislikes }
                        .thenByDescending { it.createdAt }
                )
                _allProducts.value = sortedProducts

                // Apply search filter if there's an active search
                if (_searchQuery.value.isNotEmpty()) {
                    searchProducts(_searchQuery.value)
                } else {
                    _products.value = sortedProducts
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading rising products", e)
            }
        }
    }

    fun loadProductsByDate() {
        viewModelScope.launch {
            try {
                val loadedProducts = repository.getProducts()
                val sortedProducts = loadedProducts.sortedByDescending { it.createdAt }
                _allProducts.value = sortedProducts

                // Apply search filter if there's an active search
                if (_searchQuery.value.isNotEmpty()) {
                    searchProducts(_searchQuery.value)
                } else {
                    _products.value = sortedProducts
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading products by date", e)
            }
        }
    }

    fun scrapeAmazonProduct(url: String) {
        viewModelScope.launch {
            _scrapingState.value = ScrapingState.Loading
            try {
                val imageUrls = amazonScraper.scrapeProductImages(url)
                _scrapingState.value = ScrapingState.Success(imageUrls)
            } catch (e: Exception) {
                _scrapingState.value = ScrapingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        return authRepository.getCurrentUser()
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    Log.d("ProductViewModel", "Adding product: $product")
                    val result = repository.addProduct(product)
                    if (result.isSuccess) {
                        Log.d("ProductViewModel", "Product added successfully")
                        loadProducts() // Recargar la lista de productos después de añadir uno nuevo
                    } else {
                        Log.e("ProductViewModel", "Error adding product: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.e("ProductViewModel", "User not authenticated")
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error adding product", e)
            }
        }
    }

    fun toggleLikeDislike(productId: String, isLike: Boolean) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    repository.toggleLikeDislike(productId, currentUser.id, isLike)
                    loadProducts() // Recargar la lista de productos después de cambiar un like/dislike
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error toggling like/dislike", e)
            }
        }
    }

    sealed class ScrapingState {
        object Idle : ScrapingState()
        object Loading : ScrapingState()
        data class Success(val imageUrls: List<String>) : ScrapingState()
        data class Error(val message: String) : ScrapingState()
    }

    class Factory(
        private val application: Application,
        private val repository: ProductRepository,
        private val amazonScraper: AmazonScraper,
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(application, repository, amazonScraper, authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

