package project.mobile.model

import com.google.firebase.Timestamp

/**
 * Represents a keyword alert that users can create to get notified about new products
 */
data class Alert(
    val id: String = "",
    val userId: String = "",
    val keyword: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
) 