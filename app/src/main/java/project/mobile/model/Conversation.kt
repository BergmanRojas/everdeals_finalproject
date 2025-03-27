package project.mobile.model

data class Conversation(
    val userId: String,
    val username: String,
    val handle: String,
    val lastMessage: String,
    val date: String,
    val profileImageUrl: String
)