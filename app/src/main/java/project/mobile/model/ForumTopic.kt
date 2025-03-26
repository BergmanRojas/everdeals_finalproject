package project.mobile.model

import com.google.firebase.Timestamp
import java.util.UUID

data class ForumTopic(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val userId: String,
    val userName: String,
    val createdAt: Timestamp = Timestamp.now(),
    val postCount: Int = 0,
    val lastPostAt: Timestamp? = null
)