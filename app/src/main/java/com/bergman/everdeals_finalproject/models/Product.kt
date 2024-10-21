package com.bergman.everdeals_finalproject.models

data class Product(
    val name: String,
    val productImage: String,
    val description: String,
    val link: String,
    val userId: String,
    val category: String,
    val id: String,
    val previousPrice: Double,
    val currentPrice: Double,
    val discount: Double,
    val likes: Int,
    val dislikes: Int,
    val sales: Int
)

