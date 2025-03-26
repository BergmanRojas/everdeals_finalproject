package project.mobile.model

import com.google.firebase.Timestamp

data class UserActivity(
    val type: String, // "comment", "product_shared", "like", "dislike", "forum_created"
    val content: String,
    val timestamp: Timestamp?
)