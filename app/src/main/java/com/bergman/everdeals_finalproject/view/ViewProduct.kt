package com.bergman.everdeals_finalproject.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bergman.everdeals_finalproject.models.Comment
import java.time.LocalDateTime

class ViewProduct : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val product = intent.extras ?: return

        setContent {
            val comments = remember { getSampleComments() } // Simulamos comentarios

            ViewProductScreen(
                productName = product.getString("name") ?: "",
                productImage = product.getString("image") ?: "",
                productDescription = product.getString("description") ?: "",
                currentPrice = product.getDouble("currentPrice") ?: 0.0,
                previousPrice = product.getDouble("previousPrice") ?: 0.0,
                productCategory = product.getString("category") ?: "",
                link = product.getString("link") ?: "",
                comments = comments
            )
        }
    }

    // Función que simula comentarios
    private fun getSampleComments(): List<Comment> {
        return listOf(
            Comment(userId = "Usuario1", productId = "prod1", comment = "Comentario 1", time = LocalDateTime.now()),
            Comment(userId = "Usuario2", productId = "prod2", comment = "Comentario 2", time = LocalDateTime.now())
        )
    }
}

@Composable
fun ViewProductScreen(
    productName: String,
    productImage: String,
    productDescription: String,
    currentPrice: Double,
    previousPrice: Double,
    productCategory: String,
    link: String,
    comments: List<Comment>
) {
    val context = LocalContext.current // Para manejar Intents
    Column(modifier = Modifier.padding(16.dp)) {
        // Imagen del producto
        Image(
            painter = rememberAsyncImagePainter(productImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del producto
        Text(text = productName, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Categoría del producto
        Text(text = "Category: $productCategory", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))

        // Precios del producto
        Text(text = "Previous Price: $previousPrice€", style = MaterialTheme.typography.bodyMedium, textDecoration = TextDecoration.LineThrough)
        Text(text = "Current Price: $currentPrice€", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Descripción del producto
        var expanded by remember { mutableStateOf(false) }
        val maxLines = if (expanded) Int.MAX_VALUE else 5

        Text(text = productDescription, maxLines = maxLines)
        Text(
            text = if (expanded) "Read less" else "Read more",
            modifier = Modifier.clickable { expanded = !expanded },
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para ir a la tienda
        Button(
            onClick = {
                // Usamos `LocalContext` para abrir el navegador
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go to Store")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comentarios
        Text(text = "Comments", style = MaterialTheme.typography.titleMedium)
        CommentsList(comments = comments)
    }
}

@Composable
fun CommentsList(comments: List<Comment>) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(comments) { comment ->
            CommentItem(comment = comment)
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = comment.userId, style = MaterialTheme.typography.bodyLarge)
        Text(text = comment.comment, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewViewProductScreen() {
    val sampleComments = listOf(
        Comment(userId = "Usuario1", productId = "prod1", comment = "Comentario 1", time = LocalDateTime.now()),
        Comment(userId = "Usuario2", productId = "prod2", comment = "Comentario 2", time = LocalDateTime.now())
    )

    ViewProductScreen(
        productName = "Producto de prueba",
        productImage = "https://example.com/image.jpg",
        productDescription = "Descripción del producto de prueba, que puede ser muy larga y necesita ser truncada.",
        currentPrice = 99.99,
        previousPrice = 120.00,
        productCategory = "Categoría de prueba",
        link = "https://example.com/product",
        comments = sampleComments
    )
}

