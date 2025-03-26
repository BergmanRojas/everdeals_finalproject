package project.mobile.model

import com.google.firebase.Timestamp

data class Comment(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val text: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val parentId: String? = null,
    val isReply: Boolean = false
)