package project.mobile.model

data class User(

    val following: List<String> = emptyList(), // IDs de usuarios seguidos
    val followers: List<String> = emptyList(),

    val id: String = "",
    val email: String = "",
    val username: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val backgroundUrl: String? = null
)