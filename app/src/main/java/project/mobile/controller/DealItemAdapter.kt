package project.mobile.controller

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import project.mobile.models.Product

@Composable
fun DealItemAdapter(dealViewModel: DealViewModel = viewModel(), products: List<Product>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(products) { product ->
            DealItem(product = product, dealViewModel = dealViewModel)
        }
    }
}

@Composable
fun DealItem(product: Product, dealViewModel: DealViewModel) {
    var likeCount by remember { mutableStateOf(product.likes) }
    var dislikeCount by remember { mutableStateOf(product.dislikes) }
    val context = LocalContext.current
    val userId = "current_user_id" // Reemplazar con el ID real del usuario autenticado

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { openProductLink(context, product.link) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberAsyncImagePainter(product.productImage),
                contentDescription = "Product Image",
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = product.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "€${product.currentPrice}", color = MaterialTheme.colorScheme.primary)
            Text(text = "Old Price: €${product.previousPrice}", color = MaterialTheme.colorScheme.secondary)

            Row(modifier = Modifier.padding(top = 8.dp)) {
                IconButton(onClick = {
                    dealViewModel.toggleLikeDislike(product.id, userId, true)
                    likeCount++
                }) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Like")
                }
                Text(text = likeCount.toString())

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {
                    dealViewModel.toggleLikeDislike(product.id, userId, false)
                    dislikeCount++
                }) {
                    Icon(Icons.Filled.ThumbDown, contentDescription = "Dislike")
                }
                Text(text = dislikeCount.toString())
            }
        }
    }
}

fun openProductLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Preview(showBackground = true)
@Composable
fun PreviewDealItemAdapter() {
    val fakeViewModel = DealViewModel(
        application = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application,
        repository = DealRepository()
    )

    val sampleProducts = listOf(
        Product(
            id = "1",
            name = "Smartphone",
            productImage = "https://example.com/sample.jpg",
            description = "High-end smartphone",
            category = "Electronics",
            link = "https://example.com/product",
            userId = "User123",
            previousPrice = 500.0,
            currentPrice = 399.99,
            discount = 20.0,
            likes = 50,
            dislikes = 5,
            sales = 20
        )
    )

    DealItemAdapter(dealViewModel = fakeViewModel, products = sampleProducts)
}
