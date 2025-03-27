package project.mobile.model

import com.google.firebase.Timestamp

data class UserActivity(
    val type: String,
    val content: String,
    val timestamp: Timestamp?,
    val userId: String = ""
)