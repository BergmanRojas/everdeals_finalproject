package project.mobile.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Comment(
    @DocumentId val id: String = "",
    val userId: String = "",
    val productId: String = "",
    val comment: String = "",
    val time: Timestamp = Timestamp.now() // 🔹 Firestore usa Timestamp, no LocalDateTime
)

