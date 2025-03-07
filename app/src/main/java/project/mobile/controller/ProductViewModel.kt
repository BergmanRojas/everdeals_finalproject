package project.mobile.controller

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import project.mobile.models.Product
import com.google.firebase.Timestamp

class ProductViewModel(
    application: Application,
    private val repository: DealRepository
) : AndroidViewModel(application) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _filteredProducts = MutableStateFlow<List<Product>>(emptyList())
    val filteredProducts: StateFlow<List<Product>> = _filteredProducts

    private val _scrapingState = MutableStateFlow<ScrapingState>(ScrapingState.Idle)
    val scrapingState: StateFlow<ScrapingState> = _scrapingState

    private val db = Firebase.firestore

    sealed class ScrapingState {
        object Idle : ScrapingState()
        object Loading : ScrapingState()
        data class Success(val imageUrls: List<String>) : ScrapingState()
        data class Error(val message: String) : ScrapingState()
    }

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _products.value = repository.getProducts()
            _filteredProducts.value = _products.value
        }
    }

    fun addProduct(
        name: String,
        imageUrl: String,
        amazonUrl: String,
        currentPrice: Double,
        originalPrice: Double,
        description: String?,
        category: String,
        startDate: String,
        endDate: String,
        isOnline: Boolean,
        userId: String,
        userName: String,
        userPhotoUrl: String,
        createdAt: Timestamp,
        comments: List<String>
    ) {
        viewModelScope.launch {
            try {
                val product = Product(
                    documentId = "", // Firestore generará el ID automáticamente
                    name = name,
                    imageUrl = imageUrl,
                    amazonUrl = amazonUrl, // Mapea 'link' a 'amazonUrl'
                    currentPrice = currentPrice,
                    originalPrice = originalPrice,
                    description = description,
                    category = category,
                    startDate = startDate,
                    endDate = endDate,
                    isOnline = isOnline,
                    userId = userId,
                    userName = userName,
                    userPhotoUrl = userPhotoUrl,
                    createdAt = createdAt,
                    likes = 0,
                    dislikedBy = emptyList(),
                    dislikes = 0,
                    likedBy = emptyList(),
                    comments = comments
                )
                repository.addProduct(product)
                loadProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al agregar producto: ${e.message}", e)
            }
        }
    }

    fun loadProductsByVotes() {
        viewModelScope.launch {
            _products.value = repository.getProductsByVotes()
            _filteredProducts.value = _products.value
        }
    }

    fun loadProductsByRising() {
        viewModelScope.launch {
            _products.value = repository.getProductsByRising()
            _filteredProducts.value = _products.value
        }
    }

    fun loadProductsByDate() {
        viewModelScope.launch {
            _products.value = repository.getProductsByDate()
            _filteredProducts.value = _products.value
        }
    }

    fun searchProducts(query: String) {
        if (query.isEmpty()) {
            loadProducts()
            return
        }
        viewModelScope.launch {
            _products.value = repository.searchProducts(query)
            _filteredProducts.value = _products.value
        }
    }

    fun filterProductsByCategory(categoryName: String) {
        viewModelScope.launch {
            _filteredProducts.value = if (categoryName == "All" || categoryName.isEmpty()) {
                _products.value
            } else {
                _products.value.filter { it.category == categoryName }
            }
        }
    }

        fun scrapeAmazonProduct(url: String) {
            viewModelScope.launch {
                _scrapingState.value = ScrapingState.Loading
                try {
                    val document = withContext(Dispatchers.IO) {
                        Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(30000)
                            .get()
                    }
                    val imageElements = document.select("#imgTagWrapperId img, #main-image-container img")
                    val imageUrls = imageElements.mapNotNull { it.attr("src") }.distinct()
                    if (imageUrls.isNotEmpty()) {
                        _scrapingState.value = ScrapingState.Success(imageUrls)
                    } else {
                        _scrapingState.value = ScrapingState.Error("No images found in the provided URL")
                    }
                } catch (e: Exception) {
                    _scrapingState.value = ScrapingState.Error("Failed to scrape images: ${e.message}")
                }
            }
        }

    fun toggleLikeDislike(productId: String, userId: String, isLike: Boolean) {
        viewModelScope.launch {
            try {
                val productRef = db.collection("products").document(productId)
                val productSnapshot = productRef.get().await()
                val product = productSnapshot.toObject(Product::class.java)?.copy(documentId = productSnapshot.id)

                if (product != null) {
                    val hasLiked = product.likedBy.contains(userId)
                    val hasDisliked = product.dislikedBy.contains(userId)

                    if (isLike) {
                        if (hasLiked) {
                            productRef.update(
                                "likes", FieldValue.increment(-1),
                                "likedBy", FieldValue.arrayRemove(userId)
                            ).await()
                        } else {
                            if (hasDisliked) {
                                productRef.update(
                                    "dislikes", FieldValue.increment(-1),
                                    "dislikedBy", FieldValue.arrayRemove(userId)
                                ).await()
                            }
                            productRef.update(
                                "likes", FieldValue.increment(1),
                                "likedBy", FieldValue.arrayUnion(userId)
                            ).await()
                        }
                    } else {
                        if (hasDisliked) {
                            productRef.update(
                                "dislikes", FieldValue.increment(-1),
                                "dislikedBy", FieldValue.arrayRemove(userId)
                            ).await()
                        } else {
                            if (hasLiked) {
                                productRef.update(
                                    "likes", FieldValue.increment(-1),
                                    "likedBy", FieldValue.arrayRemove(userId)
                                ).await()
                            }
                            productRef.update(
                                "dislikes", FieldValue.increment(1),
                                "dislikedBy", FieldValue.arrayUnion(userId)
                            ).await()
                        }
                    }
                    loadProducts()
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al togglear like/dislike: ${e.message}", e)
            }
        }
    }

    fun likeProduct(productId: String, userId: String) {
        viewModelScope.launch {
            try {
                db.collection("products").document(productId).update(
                    "likes", FieldValue.increment(1),
                    "likedBy", FieldValue.arrayUnion(userId)
                ).await()
                loadProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al dar like: ${e.message}", e)
            }
        }
    }

    fun unlikeProduct(productId: String, userId: String) {
        viewModelScope.launch {
            try {
                db.collection("products").document(productId).update(
                    "likes", FieldValue.increment(-1),
                    "likedBy", FieldValue.arrayRemove(userId)
                ).await()
                loadProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al quitar like: ${e.message}", e)
            }
        }
    }

    fun dislikeProduct(productId: String, userId: String) {
        viewModelScope.launch {
            try {
                db.collection("products").document(productId).update(
                    "dislikes", FieldValue.increment(1),
                    "dislikedBy", FieldValue.arrayUnion(userId)
                ).await()
                loadProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al dar dislike: ${e.message}", e)
            }
        }
    }

    fun undislikeProduct(productId: String, userId: String) {
        viewModelScope.launch {
            try {
                db.collection("products").document(productId).update(
                    "dislikes", FieldValue.increment(-1),
                    "dislikedBy", FieldValue.arrayRemove(userId)
                ).await()
                loadProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al quitar dislike: ${e.message}", e)
            }
        }
    }
}
