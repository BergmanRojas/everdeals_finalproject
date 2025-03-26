package project.mobile.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import project.mobile.R
import project.mobile.controller.AuthManager
import project.mobile.controller.ProductViewModel
import project.mobile.model.Comment
import project.mobile.model.Product
import project.mobile.ui.theme.EverdealsRed
import project.mobile.ui.theme.OrangeFF6200
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    productViewModel: ProductViewModel,
    authManager: AuthManager
) {
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val product by productViewModel.getProductById(productId).collectAsState()
    val comments by productViewModel.getCommentsForProduct(productId).collectAsState(initial = emptyList())
    var currentUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        currentUserId = authManager.getCurrentUser()?.id
    }

    LaunchedEffect(productId) {
        productViewModel.loadProductDetails(productId)
        productViewModel.loadComments(productId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A),
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1A1A)
    ) { padding ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeFF6200)
            }
        } else {
            val currentProduct = product!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Imagen del producto con gradiente
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = currentProduct.imageUrl,
                            contentDescription = "Product image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF1A1A1A)),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                    }
                }

                // Información del producto
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${currentProduct.currentPrice}€",
                                            color = EverdealsRed,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${currentProduct.originalPrice}€",
                                            color = Color.Gray,
                                            fontSize = 14.sp,
                                            textDecoration = TextDecoration.LineThrough
                                        )
                                        Surface(
                                            color = EverdealsRed.copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "-${calculateDiscount(currentProduct.originalPrice, currentProduct.currentPrice)}%",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Available at ${getStoreName(currentProduct.amazonUrl)}",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(onClick = { productViewModel.toggleLikeDislike(currentProduct.id, false) }) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            "Dislike",
                                            tint = if (currentProduct.dislikedBy.contains(currentUserId)) EverdealsRed else Color.Gray
                                        )
                                    }
                                    Text(
                                        text = "${currentProduct.likes - currentProduct.dislikes}°",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(onClick = { productViewModel.toggleLikeDislike(currentProduct.id, true) }) {
                                        Icon(
                                            Icons.Default.ArrowDropUp,
                                            "Like",
                                            tint = if (currentProduct.likedBy.contains(currentUserId)) OrangeFF6200 else Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentProduct.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (currentProduct.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentProduct.description,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentProduct.amazonUrl))
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EverdealsRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Go to deal", color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, currentProduct.amazonUrl)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share deal"))
                                    },
                                    modifier = Modifier.background(Color(0xFF2A2A2A), CircleShape)
                                ) {
                                    Icon(Icons.Default.Share, "Share", tint = Color.White)
                                }
                            }
                        }
                    }
                }

                // Sección de comentarios
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Comments",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Write a comment...", color = Color.Gray) },
                            trailingIcon = {
                                IconButton(onClick = {
                                    if (commentText.isNotBlank()) {
                                        productViewModel.addComment(productId, commentText)
                                        commentText = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Send, "Send", tint = OrangeFF6200)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2A2A2A),
                                unfocusedContainerColor = Color(0xFF2A2A2A),
                                focusedBorderColor = OrangeFF6200,
                                unfocusedBorderColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                items(comments) { comment ->
                    CommentItem(
                        comment = comment,
                        onReply = { replyText ->
                            productViewModel.replyToComment(productId, comment.id, replyText)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onReply: (String) -> Unit
) {
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.userName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTimestamp(comment.createdAt),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showReplyField = !showReplyField }
            ) {
                Text(
                    text = if (showReplyField) "Cancel reply" else "Reply",
                    color = OrangeFF6200
                )
            }
            if (showReplyField) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write a reply...", color = Color.Gray) },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (replyText.isNotBlank()) {
                                onReply(replyText)
                                replyText = ""
                                showReplyField = false
                            }
                        }) {
                            Icon(Icons.Default.Send, "Send", tint = OrangeFF6200)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A),
                        unfocusedContainerColor = Color(0xFF2A2A2A),
                        focusedBorderColor = OrangeFF6200,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }
    }
}

private fun calculateDiscount(originalPrice: Double, currentPrice: Double): Int {
    return if (originalPrice > 0) ((originalPrice - currentPrice) / originalPrice * 100).toInt() else 0
}

private fun getStoreName(url: String): String {
    return when {
        url.contains("amazon") -> "Amazon"
        url.contains("elcorteingles") -> "El Corte Inglés"
        url.contains("aliexpress") -> "AliExpress"
        else -> "Store"
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val now = System.currentTimeMillis()
    val diffInMillis = now - timestamp.seconds * 1000
    return when {
        diffInMillis < 60000 -> "Just now"
        diffInMillis < 3600000 -> "${diffInMillis / 60000}m ago"
        diffInMillis < 86400000 -> "${diffInMillis / 3600000}h ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp.seconds * 1000))
        }
    }
}

private fun getCurrentUserId(): String {
    return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
}