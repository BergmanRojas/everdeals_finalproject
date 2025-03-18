package project.mobile.view

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import project.mobile.R
import project.mobile.controller.ProductViewModel
import project.mobile.model.Comment
import project.mobile.model.Product
import project.mobile.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    comments: List<Comment>,
    onAddComment: (String) -> Unit,
    onReplyToComment: (String, String) -> Unit,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onLikeDislike: (String, Boolean) -> Unit,
    viewModel: ProductViewModel
) {
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Product Image with Gradient Overlay
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Product image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF1A1A1A)
                                    ),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                    )
                }
            }

            // Product Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Precio y descuento
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${product.currentPrice}€",
                                        color = EverdealsRed,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${product.originalPrice}€",
                                        color = Color.Gray,
                                        fontSize = 14.sp,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    Surface(
                                        color = EverdealsRed.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "-${calculateDiscount(product.originalPrice, product.currentPrice)}%",
                                            color = EverdealsRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Available at Amazon",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            // Votos
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { onLikeDislike(product.id, false) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Dislike",
                                        tint = if (product.dislikedBy.contains(getCurrentUserId())) 
                                            EverdealsRed 
                                        else 
                                            Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = "${product.likes - product.dislikes}°",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { onLikeDislike(product.id, true) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowDropUp,
                                        contentDescription = "Like",
                                        tint = if (product.likedBy.contains(getCurrentUserId())) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Título y descripción
                        Text(
                            text = product.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )

                        if (product.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.description,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botones de acción
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { 
                                    try {
                                        if (product.amazonUrl.isBlank()) {
                                            Toast.makeText(context, "Invalid URL", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.amazonUrl))
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        
                                        if (intent.resolveActivity(context.packageManager) != null) {
                                            context.startActivity(intent)
                                        } else {
                                            Toast.makeText(context, "No app found to open this link", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error opening link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EverdealsRed,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                Text(
                                    "Go to deal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Share button
                            IconButton(
                                onClick = onShare,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF2A2A2A), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }


            // Comments section with enhanced design
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Comments",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Enhanced comment input
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        placeholder = { 
                            Text(
                                "Write a comment...",
                                style = MaterialTheme.typography.bodyLarge
                            ) 
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        onAddComment(commentText)
                                        commentText = ""
                                    }
                                },
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(
                                        color = if (commentText.isNotBlank()) OrangeFF6200 else Color.Gray,
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    "Send comment",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2A2A2A),
                            unfocusedContainerColor = Color(0xFF2A2A2A),
                            disabledContainerColor = Color(0xFF2A2A2A),
                            focusedBorderColor = OrangeFF6200,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Comments list
            items(comments.filter { !it.isReply }) { comment ->
                val replies = viewModel.getRepliesForComment(product.id, comment.id)
                CommentItem(
                    comment = comment,
                    replies = replies,
                    onReply = { text -> onReplyToComment(comment.id, text) }
                )
            }
        }
    }
}

@Composable
private fun DealInfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReplyList(
    replies: List<Comment>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        replies.forEach { reply ->
            CommentItem(
                comment = reply,
                replies = emptyList(),
                onReply = { },
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CommentContent(
    comment: Comment,
    showReplyField: Boolean,
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    onReplySubmit: () -> Unit,
    onReplyClick: () -> Unit,
    replies: List<Comment>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(comment.userPhotoUrl.ifEmpty { R.drawable.default_avatar })
                    .crossfade(true)
                    .build(),
                contentDescription = "User avatar",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                fallback = painterResource(id = R.drawable.default_avatar),
                error = painterResource(id = R.drawable.default_avatar)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = comment.userName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatTimestamp(comment.createdAt),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = comment.text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 40.dp)
        )
        
        if (!comment.isReply) {
            TextButton(
                onClick = onReplyClick,
                modifier = Modifier.padding(start = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Reply,
                    contentDescription = "Reply",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reply",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showReplyField) {
        OutlinedTextField(
            value = replyText,
            onValueChange = onReplyTextChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp, top = 8.dp),
            placeholder = { Text("Write a reply...") },
            trailingIcon = {
                IconButton(onClick = onReplySubmit) {
                    Icon(Icons.Default.Send, "Send reply")
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A),
                unfocusedContainerColor = Color(0xFF2A2A2A),
                disabledContainerColor = Color(0xFF2A2A2A),
                focusedBorderColor = OrangeFF6200,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )
    }

    if (replies.isNotEmpty()) {
        ReplyList(
            replies = replies,
            modifier = Modifier.padding(start = 32.dp, top = 8.dp)
        )
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    replies: List<Comment>,
    onReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CommentContent(
            comment = comment,
            showReplyField = showReplyField,
            replyText = replyText,
            onReplyTextChange = { replyText = it },
            onReplySubmit = {
                if (replyText.isNotBlank()) {
                    onReply(replyText)
                    replyText = ""
                    showReplyField = false
                }
            },
            onReplyClick = { showReplyField = !showReplyField },
            replies = replies
        )
    }
}

private fun calculateDiscount(originalPrice: Double, currentPrice: Double): Int {
    return ((originalPrice - currentPrice) / originalPrice * 100).toInt()
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