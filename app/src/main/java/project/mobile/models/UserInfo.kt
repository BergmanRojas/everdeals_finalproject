package project.mobile.models

import com.google.firebase.firestore.DocumentId

data class UserInfo(
    @DocumentId val id: String = "",
    val name: String = "",
    val money: Int = 0,
    val sales: Int = 0,
    val profileImageUrl: String = ""
)