package com.bergman.everdeals_finalproject.models

class Product(
    var name: String,
    var link: String,
    var description: String,
    var productImage: String,
    var userId: String,
    var category: String,
    var id: String,
    var previousPrice: Double,
    var currentPrice: Double,
    var discount: Double,
    var likes: Int,
    var dislikes: Int,
    var sales: Int
)
