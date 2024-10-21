package com.bergman.everdeals_finalproject.controller

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import com.bergman.everdeals_finalproject.models.Product

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
    var likeCount by remember { mutableStateOf(product.likes) }
    var dislikeCount by remember { mutableStateOf(product.dislikes) }

    Column(modifier = Modifier.padding(8.dp)) {
        // Nombre del producto
        Text(
            text = product.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Imagen del producto
        Image(
            painter = rememberAsyncImagePainter(product.productImage),
            contentDescription = null,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        // Precio anterior y actual
        Text(
            text = "${product.previousPrice}€",
            color = Color.Gray,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )
        Text(
            text = "${product.currentPrice}€",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        // Descuento
        Text(
            text = "Descuento: ${product.discount}%",
            color = Color.Green,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Likes y Dislikes
        Row(modifier = Modifier.padding(top = 8.dp)) {
            IconButton(onClick = { likeCount++ }) {
                Icon(Icons.Filled.ThumbUp, contentDescription = "Like")
            }
            Text(text = likeCount.toString(), modifier = Modifier.alignByBaseline())

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { dislikeCount++ }) {
                Icon(Icons.Filled.ThumbDown, contentDescription = "Dislike")
            }
            Text(text = dislikeCount.toString(), modifier = Modifier.alignByBaseline())
        }

        // Botón para abrir la oferta
        Button(
            onClick = {
                val url: String = product.link
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                // Aquí no tenemos acceso a un contexto en Compose puro,
                // por lo tanto, en una actividad debes usar startActivity(intent).
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Ver Oferta")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProductItemInAdapter() {
    val sampleProduct = Product(
        name = "Producto de Ejemplo",
        productImage = "https://example.com/image.jpg",
        description = "Descripción del producto.",
        link = "https://example.com",
        userId = "user123",
        category = "Categoría de Ejemplo",
        id = "id123",
        previousPrice = 100.0,
        currentPrice = 80.0,
        discount = 20.0,
        likes = 100,
        dislikes = 10,
        sales = 50
    )
    ProductItem(product = sampleProduct, onClick = {})
}

@Preview(showBackground = true)
@Composable
fun PreviewProductListInAdapter() {
    val sampleProducts = listOf(
        Product(
            name = "Producto 1",
            productImage = "https://example.com/image1.jpg",
            description = "Descripción del producto 1.",
            link = "https://example.com/producto1",
            userId = "user1",
            category = "Categoría 1",
            id = "id1",
            previousPrice = 120.0,
            currentPrice = 100.0,
            discount = 20.0,
            likes = 50,
            dislikes = 5,
            sales = 30
        ),
        Product(
            name = "Producto 2",
            productImage = "https://example.com/image2.jpg",
            description = "Descripción del producto 2.",
            link = "https://example.com/producto2",
            userId = "user2",
            category = "Categoría 2",
            id = "id2",
            previousPrice = 150.0,
            currentPrice = 130.0,
            discount = 15.0,
            likes = 70,
            dislikes = 8,
            sales = 40
        )
    )
    ProductList(products = sampleProducts, onProductClick = {})
}
