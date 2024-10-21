package com.bergman.everdeals_finalproject.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bergman.everdeals_finalproject.models.Product

class ViewCategory : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val category = intent?.getStringExtra("category") ?: "Sin categoría"
            val products = remember { getSampleProducts() } // Productos simulados
            ViewCategoryScreen(category, products, onProductClick = {
                // Acción cuando se hace clic en un producto
                println("Clicked on product: ${it.name}")
            })
        }
    }

    // Función para obtener productos simulados
    private fun getSampleProducts(): List<Product> {
        return listOf(
            Product(
                name = "Producto 1",
                link = "https://example.com/producto1",
                description = "Descripción del producto 1",
                productImage = "https://example.com/image1.jpg",
                userId = "user1",
                category = "Categoría 1",
                id = "id1",
                previousPrice = 100.0,
                currentPrice = 80.0,
                discount = 20.0,
                likes = 50,
                dislikes = 5,
                sales = 10
            ),
            Product(
                name = "Producto 2",
                link = "https://example.com/producto2",
                description = "Descripción del producto 2",
                productImage = "https://example.com/image2.jpg",
                userId = "user2",
                category = "Categoría 1",
                id = "id2",
                previousPrice = 150.0,
                currentPrice = 130.0,
                discount = 15.0,
                likes = 70,
                dislikes = 10,
                sales = 20
            )
        )
    }
}

@Composable
fun ViewCategoryScreen(categoryName: String, products: List<Product>, onProductClick: (Product) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Categoría: $categoryName",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        ProductList(products = products, onProductClick = onProductClick)
    }
}

@Composable
fun ProductList(products: List<Product>, onProductClick: (Product) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(products) { product ->
            ProductItem(product = product, onClick = { onProductClick(product) })
        }
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(product.productImage),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Precio: ${product.currentPrice}€", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Descuento: ${product.discount}%", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewViewCategoryScreen() {
    val sampleProducts = listOf(
        Product(
            name = "Producto 1",
            link = "https://example.com/producto1",
            description = "Descripción del producto 1",
            productImage = "https://example.com/image1.jpg",
            userId = "user1",
            category = "Categoría 1",
            id = "id1",
            previousPrice = 100.0,
            currentPrice = 80.0,
            discount = 20.0,
            likes = 50,
            dislikes = 5,
            sales = 10
        ),
        Product(
            name = "Producto 2",
            link = "https://example.com/producto2",
            description = "Descripción del producto 2",
            productImage = "https://example.com/image2.jpg",
            userId = "user2",
            category = "Categoría 1",
            id = "id2",
            previousPrice = 150.0,
            currentPrice = 130.0,
            discount = 15.0,
            likes = 70,
            dislikes = 10,
            sales = 20
        )
    )
    ViewCategoryScreen(categoryName = "Electrónica", products = sampleProducts, onProductClick = {})
}
