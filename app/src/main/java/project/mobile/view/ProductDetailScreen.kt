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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import project.mobile.R
import project.mobile.controller.AuthManager
import project.mobile.controller.ProductViewModel
import project.mobile.model.Comment
import project.mobile.model.Product
import project.mobile.model.User
import project.mobile.ui.theme.EverDealsTheme
import project.mobile.ui.theme.OrangeFF6200
import project.mobile.ui.theme.RedFF0000
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    productViewModel: ProductViewModel,
    authManager: AuthManager,
    onUserClick: () -> Unit,
    navController: NavController,
) {
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val product by productViewModel.getProductById(productId).collectAsState()
    val comments by productViewModel.getCommentsForProduct(productId).collectAsState(initial = emptyList())
    var currentUserId by remember { mutableStateOf<String?>(null) }
    val currentUser = remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        currentUserId = authManager.getCurrentUser()?.id
    }

    LaunchedEffect(productId) {
        productViewModel.loadProductDetails(productId)
        productViewModel.loadComments(productId)
        productViewModel.recordProductClick(productId)
        currentUser.value = productViewModel.getCurrentUser()
    }

    EverDealsTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Product details",
                                color = OrangeFF6200,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 48.dp)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                painter = painterResource(R.drawable.back_icon),
                                contentDescription = "Back",
                                tint = OrangeFF6200
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        navigationIconContentColor = OrangeFF6200,
                        titleContentColor = OrangeFF6200
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            if (product == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeFF6200) // Progress en naranja
                }
            } else {
                val currentProduct = product!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
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
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                                            ),
                                            startY = 0f,
                                            endY = 500f
                                        )
                                    )
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
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
                                                color = RedFF0000,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${currentProduct.originalPrice}€",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                textDecoration = TextDecoration.LineThrough
                                            )
                                            Surface(
                                                color = RedFF0000.copy(alpha = 0.9f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "-${calculateDiscount(currentProduct.originalPrice, currentProduct.currentPrice)}%",
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Available at ${getStoreName(currentProduct.amazonUrl)}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                                tint = if (currentProduct.dislikedBy.contains(currentUserId)) MaterialTheme.colorScheme.tertiary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${currentProduct.likes - currentProduct.dislikes}°",
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        IconButton(onClick = { productViewModel.toggleLikeDislike(currentProduct.id, true) }) {
                                            Icon(
                                                Icons.Default.ArrowDropUp,
                                                "Like",
                                                tint = if (currentProduct.likedBy.contains(currentUserId)) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = currentProduct.name,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (currentProduct.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = currentProduct.description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RedFF0000
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Go to deal", color = MaterialTheme.colorScheme.onPrimary)
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
                                        modifier = Modifier.background(
                                            MaterialTheme.colorScheme.surface,
                                            CircleShape
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            "Share",
                                            tint = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Comments",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "Write a comment...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        if (commentText.isNotBlank()) {
                                            productViewModel.addComment(productId, commentText)
                                            commentText = ""
                                        }
                                    }) {
                                        Icon(
                                            Icons.Default.Send,
                                            "Send",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.userName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatTimestamp(comment.createdAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { showReplyField = !showReplyField }
            ) {
                Text(
                    text = if (showReplyField) "Cancel reply" else "Reply",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (showReplyField) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Write a reply...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            if (replyText.isNotBlank()) {
                                onReply(replyText)
                                replyText = ""
                                showReplyField = false
                            }
                        }) {
                            Icon(
                                Icons.Default.Send,
                                "Send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

fun calculateDiscount(originalPrice: Double, currentPrice: Double): Int {
    return if (originalPrice > 0) ((originalPrice - currentPrice) / originalPrice * 100).toInt() else 0
}

fun getStoreName(url: String): String {
    return when {
        url.contains("amazon") -> "Amazon"
        url.contains("elcorteingles") -> "El Corte Inglés"
        url.contains("aliexpress") -> "AliExpress"
        else -> "Store"
    }
}

fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
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

fun getCurrentUserId(): String {
    return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
}