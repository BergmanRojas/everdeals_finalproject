package project.mobile.model

import com.google.firebase.Timestamp

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val currentPrice: Double = 0.0,
    val originalPrice: Double = 0.0,
    val category: String = "",
    val amazonUrl: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val dislikedBy: List<String> = emptyList(),
    val comments: List<String> = emptyList(),
    val link: String = "",
    val online: Boolean = true,
    val startDate: String = "",
    val endDate: String = ""
)

