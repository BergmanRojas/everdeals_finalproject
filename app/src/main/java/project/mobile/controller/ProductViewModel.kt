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
import project.mobile.model.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.toObject
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlinx.coroutines.flow.*
import project.mobile.model.AffiliateStats
import project.mobile.model.Transaction
import project.mobile.model.TransactionStatus

class ProductViewModel(
    application: Application,
    private val repository: ProductRepository,
    private val amazonScraper: AmazonScraper,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _scrapingState = MutableStateFlow<ScrapingState>(ScrapingState.Idle)
    val scrapingState: StateFlow<ScrapingState> = _scrapingState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    private val _currentProduct = MutableStateFlow<Product?>(null)
    val currentProduct: StateFlow<Product?> = _currentProduct.asStateFlow()

    private val _affiliateStats = MutableStateFlow<List<AffiliateStats>>(emptyList())
    val affiliateStats: StateFlow<List<AffiliateStats>> = _affiliateStats.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _userBalance = MutableStateFlow(0.0)
    val userBalance: StateFlow<Double> = _userBalance.asStateFlow()

    private val _withdrawals = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val withdrawals: StateFlow<List<Map<String, Any>>> = _withdrawals.asStateFlow()

    init {
        loadProducts()
        updateCategories()
        setupCommentsListener()
        loadAffiliateStats()
        loadTransactions()
        loadWithdrawals()
        loadUserBalance()
    }

    private fun setupCommentsListener() {
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                Log.e("ProductViewModel", "No se puede configurar el listener de comentarios: Usuario no autenticado")
                return
            }

            Log.d("ProductViewModel", "Configurando listener de comentarios para usuario: ${firebaseUser.uid}")

            FirebaseFirestore.getInstance()
                .collection("comments")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ProductViewModel", "Error en el listener de comentarios: ${error.message}", error)
                        _error.value = "Error al cargar comentarios: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.d("ProductViewModel", "No hay cambios en los comentarios")
                        return@addSnapshotListener
                    }

                    viewModelScope.launch {
                        try {
                            Log.d("ProductViewModel", "Procesando ${snapshot.documents.size} comentarios")
                            val commentsMap = mutableMapOf<String, MutableList<Comment>>()

                            for (doc in snapshot.documents) {
                                val comment = doc.toObject(Comment::class.java)
                                if (comment != null) {
                                    val productComments = commentsMap.getOrPut(comment.productId) { mutableListOf() }
                                    productComments.add(comment)
                                    Log.d("ProductViewModel", "Comentario procesado: ${comment.id} para producto ${comment.productId}")
                                }
                            }

                            commentsMap.values.forEach { comments ->
                                comments.sortByDescending { it.createdAt }
                            }

                            _comments.value = commentsMap
                            Log.d("ProductViewModel", "Estado de comentarios actualizado: ${commentsMap.size} productos con comentarios")
                        } catch (e: Exception) {
                            Log.e("ProductViewModel", "Error procesando comentarios: ${e.message}", e)
                            _error.value = "Error procesando comentarios: ${e.message}"
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("ProductViewModel", "Error al configurar el listener de comentarios: ${e.message}", e)
            _error.value = "Error al configurar el listener de comentarios: ${e.message}"
        }
    }

    private fun updateCategories() {
        viewModelScope.launch {
            val uniqueCategories = _allProducts.value
                .map { it.category }
                .distinct()
                .sorted()
            _categories.value = uniqueCategories
        }
    }

    fun setSelectedCategory(category: String?) {
        viewModelScope.launch {
            _selectedCategory.value = category
            filterProducts()
        }
    }

    private fun filterProducts() {
        val filteredProducts = _allProducts.value.filter { product ->
            val matchesSearch = if (_searchQuery.value.isEmpty()) {
                true
            } else {
                product.name.contains(_searchQuery.value, ignoreCase = true) ||
                product.description.contains(_searchQuery.value, ignoreCase = true) ||
                product.category.contains(_searchQuery.value, ignoreCase = true)
            }

            val matchesCategory = if (_selectedCategory.value == null) {
                true
            } else {
                product.category == _selectedCategory.value
            }

            matchesSearch && matchesCategory
        }
        Log.d("ProductViewModel", "Productos filtrados: ${filteredProducts.size}")
        _products.value = filteredProducts
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            filterProducts()
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                val loadedProducts = repository.getProducts()
                _allProducts.value = loadedProducts
                updateCategories()
                filterProducts()
            } catch (e: Exception) {
                _error.value = "Error al cargar productos: ${e.message}"
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
                Log.d("ProductViewModel", "Productos ordenados por votos: ${sortedProducts.size}")
                filterProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading products by votes", e)
                _error.value = "Error al cargar productos por votos: ${e.message}"
            }
        }
    }

    fun loadProductsByRising() {
        viewModelScope.launch {
            try {
                val loadedProducts = repository.getProducts()
                val sortedProducts = loadedProducts.sortedWith(
                    compareByDescending<Product> { it.likes - it.dislikes }
                        .thenByDescending { it.createdAt }
                )
                _allProducts.value = sortedProducts
                Log.d("ProductViewModel", "Productos ordenados por rising: ${sortedProducts.size}")
                filterProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading rising products", e)
                _error.value = "Error al cargar productos rising: ${e.message}"
            }
        }
    }

    fun loadProductsByDate() {
        viewModelScope.launch {
            try {
                val loadedProducts = repository.getProducts()
                val sortedProducts = loadedProducts.sortedByDescending { it.createdAt }
                _allProducts.value = sortedProducts
                Log.d("ProductViewModel", "Productos ordenados por fecha: ${sortedProducts.size}")
                filterProducts()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading products by date", e)
                _error.value = "Error al cargar productos por fecha: ${e.message}"
            }
        }
    }

    fun scrapeAmazonProduct(url: String) {
        viewModelScope.launch {
            _scrapingState.value = ScrapingState.Loading
            try {
                val scrapedData = amazonScraper.scrapeProductData(url)
                _scrapingState.value = ScrapingState.Success(
                    imageUrls = scrapedData.imageUrls,
                    category = scrapedData.category
                )
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
                        loadProducts()
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
                if (currentUser == null) {
                    _error.value = "Debes iniciar sesión para votar"
                    return@launch
                }

                repository.toggleLikeDislike(productId, currentUser.id, isLike)
                loadProductDetails(productId)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error toggling like/dislike", e)
                _error.value = "Error al votar"
            }
        }
    }

    fun getCommentsForProduct(productId: String): StateFlow<List<Comment>> {
        return comments
            .map { it[productId] ?: emptyList() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )
    }

    fun addComment(productId: String, text: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _error.value = "Debes iniciar sesión para comentar"
                    return@launch
                }

                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    userId = currentUser.id,
                    userName = currentUser.name,
                    userPhotoUrl = currentUser.photoUrl ?: "",
                    text = text,
                    createdAt = Timestamp.now()
                )

                repository.addComment(comment)
                loadComments(productId)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error adding comment", e)
                _error.value = "Error al agregar el comentario"
            }
        }
    }

    fun replyToComment(productId: String, commentId: String, text: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _error.value = "Debes iniciar sesión para responder"
                    return@launch
                }

                val reply = Comment(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    userId = currentUser.id,
                    userName = currentUser.name,
                    userPhotoUrl = currentUser.photoUrl ?: "",
                    text = text,
                    createdAt = Timestamp.now(),
                    parentId = commentId,
                    isReply = true
                )

                repository.addComment(reply)
                loadComments(productId)
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error replying to comment", e)
                _error.value = "Error al responder al comentario"
            }
        }
    }

    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            try {
                val product = repository.getProductById(productId)
                _currentProduct.value = product
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading product details", e)
                _currentProduct.value = null
            }
        }
    }

    fun loadComments(productId: String) {
        viewModelScope.launch {
            try {
                Log.d("ProductViewModel", "Loading comments for product: $productId")
                val productComments = repository.getCommentsForProduct(productId)
                Log.d("ProductViewModel", "Loaded ${productComments.size} comments")
                val currentComments = _comments.value.toMutableMap()
                currentComments[productId] = productComments
                _comments.value = currentComments
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading comments", e)
                _error.value = "Error loading comments: ${e.message}"
            }
        }
    }

    fun getProductById(productId: String): StateFlow<Product?> {
        return _currentProduct.asStateFlow()
    }

    fun loadAffiliateStats() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                Log.d("ProductViewModel", "Cargando estadísticas de afiliados para usuario: $userId")
                val stats = repository.getAffiliateStatsForUser(userId)
                _affiliateStats.value = stats
                Log.d("ProductViewModel", "Estadísticas cargadas: ${stats.size} productos")
                // Actualizar el balance después de cargar las estadísticas
                loadUserBalance()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al cargar estadísticas de afiliados", e)
                _error.value = "Error al cargar estadísticas: ${e.message}"
            }
        }
    }

    fun recordProductClick(productId: String) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                Log.d("ProductViewModel", "Registrando click para producto: $productId")
                repository.incrementProductClicks(productId, userId)
                // Recargar estadísticas después de registrar el click
                loadAffiliateStats()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al registrar click", e)
                _error.value = "Error al registrar click: ${e.message}"
            }
        }
    }

    // Esta función será llamada desde el panel de administración
    fun updateProductSales(productId: String, newSales: Int) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                Log.d("ProductViewModel", "Actualizando ventas para producto: $productId")
                repository.updateProductSales(productId, userId, newSales)
                // Recargar estadísticas después de actualizar las ventas
                loadAffiliateStats()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al actualizar ventas", e)
                _error.value = "Error al actualizar ventas: ${e.message}"
            }
        }
    }

    fun recordProductSale(productId: String, saleAmount: Double) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                repository.recordProductSale(productId, userId, saleAmount)
                loadAffiliateStats()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error recording sale", e)
                _error.value = e.message ?: "Error recording sale"
            }
        }
    }

    fun loadTransactions() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                val transactions = repository.getTransactions(userId)
                _transactions.value = transactions
                // Actualizar el balance después de cargar las transacciones
                loadUserBalance()
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading transactions", e)
                _error.value = "Error loading transactions: ${e.message}"
            }
        }
    }

    fun loadWithdrawals() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                val withdrawalsList = repository.getWithdrawals(userId)
                _withdrawals.value = withdrawalsList
                loadUserBalance() // Actualizar el balance después de cargar los retiros
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading withdrawals", e)
                _error.value = "Error loading withdrawals: ${e.message}"
            }
        }
    }

    fun loadUserBalance() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                // Calcular el balance total sumando las ganancias de todos los productos
                val totalEarnings = _affiliateStats.value.sumOf { it.earnings }
                // Obtener el total de retiros completados
                val completedWithdrawals = _withdrawals.value
                    .filter { it["status"] == "COMPLETED" }
                    .sumOf { it["amount"] as? Double ?: 0.0 }
                // El balance disponible es el total menos los retiros completados
                _userBalance.value = totalEarnings - completedWithdrawals
                Log.d("ProductViewModel", "Balance actualizado: ${_userBalance.value}")
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error loading user balance", e)
                _error.value = "Error loading balance: ${e.message}"
            }
        }
    }

    fun requestWithdrawal(amount: Double, paypalEmail: String) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.id ?: return@launch
                val result = repository.createWithdrawalRequest(userId, amount, paypalEmail)
                
                result.onSuccess {
                    // Recargar todo después de un retiro exitoso
                    loadWithdrawals()
                    loadTransactions()
                    loadUserBalance()
                }.onFailure { e ->
                    _error.value = e.message ?: "Error requesting withdrawal"
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error requesting withdrawal", e)
                _error.value = "Error requesting withdrawal: ${e.message}"
            }
        }
    }

    sealed class ScrapingState {
        object Idle : ScrapingState()
        object Loading : ScrapingState()
        data class Success(val imageUrls: List<String>, val category: String) : ScrapingState()
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

