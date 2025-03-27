package project.mobile.model

import com.google.firebase.Timestamp

data class AffiliateStats(
    val productId: String = "",
    val userId: String = "",
    val clicks: Int = 0,
    val sales: Int = 0,
    val earnings: Double = 0.0,
    val productName: String = "",
    val productImage: String = "",
    val productPrice: Double = 0.0
) 