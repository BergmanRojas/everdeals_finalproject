package com.bergman.everdeals_finalproject.controller

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bergman.everdeals_finalproject.models.Category

@Composable
fun CategoriesList(categories: List<Category>, onCategoryClick: (Category) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(categories) { category ->
            CategoryItem(category = category, onClick = { onCategoryClick(category) })
        }
    }
}

@Composable
fun CategoryItem(category: Category, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Imagen de la categoría
        Image(
            painter = rememberAsyncImagePainter(category.imageUrl),
            contentDescription = category.name,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Nombre de la categoría
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoryItem() {
    val sampleCategory = Category(
        name = "Tecnología",
        imageUrl = "https://example.com/category_image.jpg"
    )
    CategoryItem(category = sampleCategory, onClick = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewCategoriesList() {
    val sampleCategories = listOf(
        Category(name = "Electrónica", imageUrl = "https://example.com/electronics.jpg"),
        Category(name = "Moda", imageUrl = "https://example.com/fashion.jpg"),
        Category(name = "Hogar", imageUrl = "https://example.com/home.jpg")
    )
    CategoriesList(categories = sampleCategories, onCategoryClick = {})
}
