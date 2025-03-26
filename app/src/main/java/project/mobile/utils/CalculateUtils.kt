package project.mobile.utils

fun calculateDiscount(originalPrice: Double, currentPrice: Double): Int {
    return if (originalPrice > 0) ((originalPrice - currentPrice) / originalPrice * 100).toInt() else 0
}