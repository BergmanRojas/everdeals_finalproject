package project.mobile.models

data class Category(
    val id: String = "",
    val name: String = "",
    val subcategories: List<String> = emptyList()
)