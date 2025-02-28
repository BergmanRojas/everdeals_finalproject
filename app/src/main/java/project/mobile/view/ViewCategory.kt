package project.mobile.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import project.mobile.models.Product
import androidx.navigation.NavController
import project.mobile.view.Screen

@Composable
fun ViewCategoryScreen(
    navController: NavController,
    categoryName: String,
    products: List<Product>
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Categoría: $categoryName",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ProductList(products = products, onProductClick = { product ->
            navController.navigate(Screen.ViewProduct.createRoute(product.id))
        })
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
            contentDescription = "Imagen del producto",
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
            id = "id1",
            name = "Producto 1",
            productImage = "https://example.com/image1.jpg",
            description = "Descripción del producto 1",
            category = "Electrónica",
            link = "https://example.com/producto1",
            userId = "user1",
            previousPrice = 100.0,
            currentPrice = 80.0,
            discount = 20.0,
            likes = 50,
            dislikes = 5,
            sales = 10
        ),
        Product(
            id = "id2",
            name = "Producto 2",
            productImage = "https://example.com/image2.jpg",
            description = "Descripción del producto 2",
            category = "Electrónica",
            link = "https://example.com/producto2",
            userId = "user2",
            previousPrice = 150.0,
            currentPrice = 130.0,
            discount = 15.0,
            likes = 70,
            dislikes = 10,
            sales = 20
        )
    )

    // `navController` en la previsualización se deja en `null`
    ViewCategoryScreen(navController = NavController(LocalContext.current), categoryName = "Electrónica", products = sampleProducts)
}
