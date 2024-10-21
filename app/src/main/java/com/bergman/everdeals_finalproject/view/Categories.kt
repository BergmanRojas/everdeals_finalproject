package com.bergman.everdeals_finalproject.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bergman.everdeals_finalproject.controller.CategoriesList
import com.bergman.everdeals_finalproject.models.Category

class Categories : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Llama a la función Compose para mostrar las categorías
            val categories = remember { getSampleCategories() }
            CategoriesScreen(categories = categories, onCategoryClick = { category ->
                println("Clicked on category: ${category.name}")
            })
        }
    }

    // Función para generar categorías de ejemplo (sin Firebase)
    private fun getSampleCategories(): List<Category> {
        return listOf(
            Category(imageUrl = "https://example.com/image1.jpg", name = "Tecnología"),
            Category(imageUrl = "https://example.com/image2.jpg", name = "Moda"),
            Category(imageUrl = "https://example.com/image3.jpg", name = "Hogar")
        )
    }
}

@Composable
fun CategoriesScreen(categories: List<Category>, onCategoryClick: (Category) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Categorías",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        CategoriesList(categories = categories, onCategoryClick = onCategoryClick)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoriesScreen() {
    val sampleCategories = listOf(
        Category(imageUrl = "https://example.com/image1.jpg", name = "Tecnología"),
        Category(imageUrl = "https://example.com/image2.jpg", name = "Moda"),
        Category(imageUrl = "https://example.com/image3.jpg", name = "Hogar")
    )
    CategoriesScreen(categories = sampleCategories, onCategoryClick = {})
}
