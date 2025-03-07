package project.mobile.view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import project.mobile.controller.ProductViewModel
import project.mobile.models.Product

@Composable
fun ProductItem(
    product: Product,
    dealViewModel: ProductViewModel,
    onProductClick: (String) -> Unit
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    var likeCount by remember { mutableStateOf(product.likes) }
    var dislikeCount by remember { mutableStateOf(product.dislikes) }
    var hasLiked by remember { mutableStateOf(product.likedBy.contains(userId)) }
    var hasDisliked by remember { mutableStateOf(product.dislikedBy.contains(userId)) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onProductClick(product.documentId) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.imageUrl),
                contentDescription = "Product Image",
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "€${product.currentPrice}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "Old Price: €${product.originalPrice}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.LineThrough
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            if (hasLiked) {
                                dealViewModel.unlikeProduct(product.documentId, userId)
                                likeCount -= 1
                                hasLiked = false
                            } else {
                                if (hasDisliked) {
                                    dealViewModel.undislikeProduct(product.documentId, userId)
                                    dislikeCount -= 1
                                    hasDisliked = false
                                }
                                dealViewModel.likeProduct(product.documentId, userId)
                                likeCount += 1
                                hasLiked = true
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (hasLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (hasLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = likeCount.toString(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = {
                        scope.launch {
                            if (hasDisliked) {
                                dealViewModel.undislikeProduct(product.documentId, userId)
                                dislikeCount -= 1
                                hasDisliked = false
                            } else {
                                if (hasLiked) {
                                    dealViewModel.unlikeProduct(product.documentId, userId)
                                    likeCount -= 1
                                    hasLiked = false
                                }
                                dealViewModel.dislikeProduct(product.documentId, userId)
                                dislikeCount += 1
                                hasDisliked = true
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (hasDisliked) Icons.Filled.ThumbDown else Icons.Filled.ThumbDownOffAlt,
                        contentDescription = "Dislike",
                        tint = if (hasDisliked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = dislikeCount.toString(),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}