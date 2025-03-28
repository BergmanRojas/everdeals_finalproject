package project.mobile.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import project.mobile.model.Product
import project.mobile.model.UserActivity

class StatsViewModel(
    private val authManager: AuthManager
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _stats = MutableStateFlow<List<String>>(emptyList())
    val stats: StateFlow<List<String>> = _stats

    private val _activities = MutableStateFlow<List<UserActivity>>(emptyList())
    val activities: StateFlow<List<UserActivity>> = _activities

    private val _products = MutableStateFlow<List<Product>>(emptyList()) // Añadido
    val products: StateFlow<List<Product>> = _products // Añadido

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    fun loadStats(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _fetchError.value = null
            Log.d("StatsViewModel", "Starting to load stats for userId: $userId")

            try {
                // Cargar actividades
                val activitySnapshot = try {
                    Log.d("StatsViewModel", "Fetching activities from server for userId: $userId")
                    firestore.collection("user_interactions")
                        .whereEqualTo("userId", userId)
                        .get(Source.SERVER)
                        .await()
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("StatsViewModel", "Permission denied for activities of userId: $userId")
                        _fetchError.value = "Permission denied to access activities"
                        return@launch
                    } else if (e.message?.contains("offline") == true) {
                        Log.w("StatsViewModel", "Offline, loading activities from cache for userId: $userId")
                        firestore.collection("user_interactions")
                            .whereEqualTo("userId", userId)
                            .get(Source.CACHE)
                            .await()
                    } else {
                        throw e
                    }
                }

                val comments = activitySnapshot.documents
                    .filter { it.getString("type") == "comment" }
                    .map { doc ->
                        UserActivity(
                            type = "comment",
                            content = doc.getString("comment") ?: "No comment",
                            timestamp = doc.getTimestamp("createdAt"),
                            userId = doc.getString("userId") ?: ""
                        )
                    }
                _activities.value = comments

                // Cargar productos (deals)
                val productSnapshot = try {
                    Log.d("StatsViewModel", "Fetching products from server for userId: $userId")
                    firestore.collection("products")
                        .whereEqualTo("userId", userId)
                        .get(Source.SERVER)
                        .await()
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("StatsViewModel", "Permission denied for products of userId: $userId")
                        _fetchError.value = "Permission denied to access products"
                        return@launch
                    } else if (e.message?.contains("offline") == true) {
                        Log.w("StatsViewModel", "Offline, loading products from cache for userId: $userId")
                        firestore.collection("products")
                            .whereEqualTo("userId", userId)
                            .get(Source.CACHE)
                            .await()
                    } else {
                        throw e
                    }
                }

                val deals = productSnapshot.documents.map { doc ->
                    Product(
                        id = doc.getString("id") ?: "",
                        name = doc.getString("name") ?: "No name",
                        description = doc.getString("description") ?: "",
                        currentPrice = doc.getDouble("currentPrice") ?: 0.0,
                        originalPrice = doc.getDouble("originalPrice") ?: 0.0,
                        imageUrl = doc.getString("imageUrl") ?: "",
                        amazonUrl = doc.getString("amazonUrl") ?: "",
                        category = doc.getString("category") ?: "",
                        startDate = doc.getString("startDate") ?: "",
                        endDate = doc.getString("endDate") ?: "",
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        likes = (doc.getLong("likes") ?: 0L).toInt(),
                        dislikes = (doc.getLong("dislikes") ?: 0L).toInt(),
                        likedBy = doc.get("likedBy") as? List<String> ?: emptyList(),
                        dislikedBy = doc.get("dislikedBy") as? List<String> ?: emptyList(),
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                    )
                }
                _products.value = deals // Almacenar los productos

                // Statistics
                val totalProducts = deals.size
                val totalLikesReceived = deals.sumOf { it.likes }
                val totalComments = comments.size
                _stats.value = listOf(
                    "Products shared: $totalProducts",
                    "Likes received: $totalLikesReceived",
                    "Comments made: $totalComments"
                )

            } catch (e: Exception) {
                Log.e("StatsViewModel", "Error fetching stats: ${e.message}", e)
                _fetchError.value = "Error loading stats: ${e.message}"
            }

            Log.d("StatsViewModel", "Stats load completed for userId: $userId")
            _isLoading.value = false
        }
    }
}