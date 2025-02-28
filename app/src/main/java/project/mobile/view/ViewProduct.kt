package project.mobile.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import project.mobile.controller.CommentList
import project.mobile.controller.DealViewModel
import project.mobile.models.Comment
import project.mobile.models.Product
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ViewProductScreen(
    dealViewModel: DealViewModel = viewModel(),
    commentViewModel: CommentViewModel = viewModel(), // Instanciamos el ViewModel de comentarios
    product: Product
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        Image(
            painter = rememberAsyncImagePainter(product.productImage),
            contentDescription = "Product Image",
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = product.name, style = MaterialTheme.typography.headlineMedium)
        Text(text = "Category: ${product.category}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Posted by: ${product.userId}", style = MaterialTheme.typography.bodyMedium)
        Text(text = "Published: ${formatTimestamp(product.createdAt)}", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Previous Price: ${product.previousPrice}€", textDecoration = TextDecoration.LineThrough)
        Text(text = "Current Price: ${product.currentPrice}€", color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(product.link))) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Go to Deal")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // **Aquí se agregan los comentarios usando CommentViewModel**
        Text(text = "Comments", style = MaterialTheme.typography.titleMedium)
        CommentList(commentViewModel = commentViewModel, productId = product.id)
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
        Text(text = formatTimestamp(comment.time), style = MaterialTheme.typography.bodySmall)
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@Preview(showBackground = true)
@Composable
fun PreviewViewProductScreen() {
    val sampleProduct = Product(
        id = "1",
        name = "Sample Product",
        productImage = "https://example.com/sample.jpg",
        description = "Description of the sample product.",
        currentPrice = 99.99,
        previousPrice = 120.00,
        category = "Sample Category",
        link = "https://example.com/product",
        userId = "User123",
        discount = 10.0,
        likes = 50,
        dislikes = 5,
        sales = 20,
        createdAt = Timestamp.now()
    )

    val sampleComments = listOf(
        Comment(userId = "User1", productId = "1", comment = "Nice product!", time = Timestamp.now()),
        Comment(userId = "User2", productId = "1", comment = "Great deal!", time = Timestamp.now())
    )

    ViewProductScreen(product = sampleProduct, comments = sampleComments)
}
