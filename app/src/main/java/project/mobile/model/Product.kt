package project.mobile.model

import com.google.firebase.Timestamp

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val originalPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val imageUrl: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val amazonUrl: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val category: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList()
)

