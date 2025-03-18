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

    init {
        loadProducts()
        updateCategories()
        setupCommentsListener()
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

                            // Sort comments by timestamp
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
                Log.d("ProductViewModel", "Loading products...")
                val loadedProducts = repository.getProducts()
                Log.d("ProductViewModel", "Products loaded: ${loadedProducts.size}")
                _allProducts.value = loadedProducts
                updateCategories()
                filterProducts()
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

    fun getCommentsForProduct(productId: String): StateFlow<List<Comment>> = _comments
        .map { it[productId] ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addComment(productId: String, text: String) {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                Log.d("ProductViewModel", "Firebase User: ${firebaseUser?.uid}")
                
                if (firebaseUser == null) {
                    Log.e("ProductViewModel", "Usuario no autenticado")
                    _error.value = "Usuario no autenticado"
                    return@launch
                }

                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    userId = firebaseUser.uid,
                    userName = firebaseUser.displayName ?: "Anonymous",
                    userPhotoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    text = text,
                    createdAt = Timestamp.now()
                )

                try {
                    val db = FirebaseFirestore.getInstance()
                    Log.d("ProductViewModel", "Intentando guardar comentario: ${comment.id}")
                    
                    // Primero, añadir el comentario a la colección de comentarios
                    db.collection("comments")
                        .document(comment.id)
                        .set(comment)
                        .await()

                    Log.d("ProductViewModel", "Comentario guardado en Firestore: ${comment.id}")

                    // Actualizar el estado local después de que todo se haya guardado
                    val currentComments = _comments.value.toMutableMap()
                    val productComments = currentComments[productId]?.toMutableList() ?: mutableListOf()
                    productComments.add(0, comment)
                    currentComments[productId] = productComments
                    _comments.value = currentComments
                    
                    Log.d("ProductViewModel", "Estado local actualizado")
                    
                } catch (e: Exception) {
                    Log.e("ProductViewModel", "Error al guardar el comentario: ${e.message}", e)
                    _error.value = "Error al guardar el comentario: ${e.message}"
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error de autenticación: ${e.message}", e)
                _error.value = "Error de autenticación: ${e.message}"
            }
        }
    }

    fun addReply(productId: String, parentCommentId: String, text: String) {
        viewModelScope.launch {
            try {
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                Log.d("ProductViewModel", "Firebase User: ${firebaseUser?.uid}")
                
                if (firebaseUser == null) {
                    Log.e("ProductViewModel", "Usuario no autenticado")
                    _error.value = "Usuario no autenticado"
                    return@launch
                }

                val reply = Comment(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    userId = firebaseUser.uid,
                    userName = firebaseUser.displayName ?: "Anonymous",
                    userPhotoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    text = text,
                    createdAt = Timestamp.now(),
                    parentId = parentCommentId,
                    isReply = true
                )

                try {
                    val db = FirebaseFirestore.getInstance()
                    Log.d("ProductViewModel", "Intentando guardar respuesta: ${reply.id}")
                    
                    db.collection("comments")
                        .document(reply.id)
                        .set(reply)
                        .await()

                    Log.d("ProductViewModel", "Respuesta guardada en Firestore: ${reply.id}")

                    // Actualizar el estado local
                    val currentComments = _comments.value.toMutableMap()
                    val productComments = currentComments[productId]?.toMutableList() ?: mutableListOf()
                    productComments.add(0, reply)
                    currentComments[productId] = productComments
                    _comments.value = currentComments
                    
                    Log.d("ProductViewModel", "Estado local actualizado con la respuesta")
                    
                } catch (e: Exception) {
                    Log.e("ProductViewModel", "Error al guardar la respuesta: ${e.message}", e)
                    _error.value = "Error al guardar la respuesta: ${e.message}"
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error de autenticación: ${e.message}", e)
                _error.value = "Error de autenticación: ${e.message}"
            }
        }
    }

    // Función auxiliar para obtener las respuestas de un comentario
    fun getRepliesForComment(productId: String, commentId: String): List<Comment> {
        return _comments.value[productId]?.filter { it.parentId == commentId && it.isReply } ?: emptyList()
    }

    fun getProductById(productId: String): Product? {
        return _allProducts.value.find { it.id == productId }
    }

    sealed class ScrapingState {
        object Idle : ScrapingState()
        object Loading : ScrapingState()
        data class Success(
            val imageUrls: List<String>,
            val category: String
        ) : ScrapingState()
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

