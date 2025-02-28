package project.mobile.models

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId val id: String = "",
    val imageUrl: String = "",
    val name: String = ""
)