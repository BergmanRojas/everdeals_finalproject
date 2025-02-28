package project.mobile.models

import com.google.firebase.firestore.DocumentId

data class DrawerItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val iconId: Int = 0
)