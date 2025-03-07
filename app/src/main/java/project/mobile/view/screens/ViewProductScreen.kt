package project.mobile.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import project.mobile.controller.CommentViewModel
import project.mobile.controller.ProductViewModel
import project.mobile.models.Product
import project.mobile.view.components.CommentList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProductScreen(
    dealViewModel: ProductViewModel,
    product: Product,
    commentViewModel: CommentViewModel = viewModel()
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
    var commentText by remember { mutableStateOf("") }
    var likeCount by remember { mutableStateOf(product.likes) }
    var dislikeCount by remember { mutableStateOf(product.dislikes) }
    var hasLiked by remember { mutableStateOf(product.likedBy.contains(userId)) }
    var hasDisliked by remember { mutableStateOf(product.dislikedBy.contains(userId)) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product.name, color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Image(
                    painter = rememberAsyncImagePainter(product.imageUrl),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Price: €${product.currentPrice}",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "Old Price: €${product.originalPrice}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(16.dp))

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
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Comments",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CommentList(commentViewModel = commentViewModel, productId = product.documentId)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Add a comment") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (commentText.isNotEmpty()) {
                                scope.launch {
                                    commentViewModel.addComment(product.documentId, userId, commentText)
                                    commentText = ""
                                }
                            }
                        }) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send comment",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}