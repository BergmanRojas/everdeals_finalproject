package project.mobile.models

import com.google.firebase.Timestamp

data class Product(
    val documentId: String = "", // Cambiado de 'id' a 'documentId'
    val name: String = "",
    val category: String = "",
    val currentPrice: Double = 0.0,
    val originalPrice: Double = 0.0,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikes: Int = 0,
    val dislikedBy: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val amazonUrl: String? = null,
    val endDate: String? = null,
    val startDate: String? = null,
    val comments: List<String>? = null,
    val isOnline: Boolean = false,
    val userId: String? = null,
    val userName: String? = null,
    val userPhotoUrl: String? = null
)