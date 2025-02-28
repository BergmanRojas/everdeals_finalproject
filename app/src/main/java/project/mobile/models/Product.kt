package project.mobile.models

import com.google.firebase.Timestamp

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val originalPrice: Double = 0.0,
    val currentPrice: Double = 0.0,
    val productImage: String = "",
    val userId: String = "",
    val category: String = "",
    val link: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val dislikes: Int = 0,
    val likedBy: MutableList<String> = mutableListOf(),
    val dislikedBy: MutableList<String> = mutableListOf(),
    val previousPrice: Double = 0.0,
    val discount: Double = 0.0,
    val sales: Int = 0
)
