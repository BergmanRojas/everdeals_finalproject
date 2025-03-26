package project.mobile.model

import com.google.firebase.Timestamp
import java.util.UUID

data class Notification(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: String,
    val message: String,
    val relatedId: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
)