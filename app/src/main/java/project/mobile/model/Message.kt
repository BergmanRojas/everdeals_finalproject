package project.mobile.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    @get:PropertyName("read") @set:PropertyName("read") var isRead: Boolean = false
)