package project.mobile.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import project.mobile.getStoreName
import project.mobile.model.Product
import project.mobile.ui.theme.EverdealsRed
import project.mobile.ui.theme.OrangeFF6200
import project.mobile.ui.theme.Purple9046FF
import project.mobile.ui.theme.YellowFFE100
import project.mobile.utils.calculateDiscount
import project.mobile.utils.formatTimestamp
import project.mobile.utils.getCurrentUserId

@Composable
fun DealCard(
    product: Product,
    onLikeDislike: (String, Boolean) -> Unit,
    onClick: () -> Unit,
    onUserClick: (String) -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val shadowColor = if (isDarkTheme) YellowFFE100 else Purple9046FF
    val currentUserId = getCurrentUserId()
    val db = FirebaseFirestore.getInstance()
    var isSaved by remember { mutableStateOf(false) }

    // Ajustar el color del nombre del usuario según el tema
    val userNameColor = if (isDarkTheme) YellowFFE100 else Purple9046FF

    // Obtener la indicación (efecto visual) dentro del contexto @Composable
    val indication = remember { ripple() }

    // Verificar si está guardado
    LaunchedEffect(product.id) {
        if (currentUserId.isNotEmpty()) {
            try {
                db.collection("users").document(currentUserId)
                    .collection("savedPosts").document(product.id)
                    .get()
                    .addOnSuccessListener { isSaved = it.exists() }
                    .addOnFailureListener { e ->
                        Log.e("DealCard", "Error checking saved post: ${e.message}", e)
                        isSaved = false // Valor por defecto en caso de error
                    }
            } catch (e: Exception) {
                Log.e("DealCard", "Firestore offline or error: ${e.message}", e)
                isSaved = false
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = shadowColor)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Product image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopEnd),
                        color = EverdealsRed.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "-${calculateDiscount(product.originalPrice, product.currentPrice)}%",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = product.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = product.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${product.currentPrice}€",
                            color = EverdealsRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${product.originalPrice}€",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = getStoreName(product.amazonUrl),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AsyncImage(
                        model = product.userPhotoUrl.ifEmpty { "https://via.placeholder.com/32" },
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.userName,
                            color = userNameColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable(
                                    enabled = product.userId.isNotEmpty(),
                                    indication = indication,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    onUserClick(product.userId)
                                }
                        )

                        Text(
                            text = " · ${formatTimestamp(product.createdAt)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, // Corregido de onSurfaceVariance a onSurfaceVariant
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (currentUserId.isEmpty()) {
                            Toast.makeText(context, "Please sign in to save posts", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        if (isSaved) {
                            db.collection("users")
                                .document(currentUserId)
                                .collection("savedPosts")
                                .document(product.id)
                                .delete()
                                .addOnSuccessListener {
                                    isSaved = false
                                    Toast.makeText(context, "Post removed from saved", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DealCard", "Error removing saved post: ${e.message}", e)
                                    Toast.makeText(context, "Error removing post", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            val savedPost = hashMapOf(
                                "productId" to product.id,
                                "title" to product.name,
                                "imageUrl" to product.imageUrl,
                                "savedAt" to Timestamp.now()
                            )

                            db.collection("users")
                                .document(currentUserId)
                                .collection("savedPosts")
                                .document(product.id)
                                .set(savedPost)
                                .addOnSuccessListener {
                                    isSaved = true
                                    Toast.makeText(context, "Post saved", Toast.LENGTH_SHORT).show()

                                    val activity = hashMapOf(
                                        "userId" to currentUserId,
                                        "action" to "saved",
                                        "productId" to product.id,
                                        "title" to product.name,
                                        "timestamp" to Timestamp.now()
                                    )
                                    db.collection("users")
                                        .document(currentUserId)
                                        .collection("activities")
                                        .add(activity)
                                        .addOnFailureListener { e ->
                                            Log.e("DealCard", "Error adding activity: ${e.message}", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DealCard", "Error saving post: ${e.message}", e)
                                    Toast.makeText(context, "Error saving post", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Guardar",
                        tint = if (isSaved) OrangeFF6200 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "${product.comments?.size ?: 0}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onLikeDislike(product.id, true) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowDropUp,
                            contentDescription = "Like",
                            tint = if (product.likedBy.contains(currentUserId))
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${product.likes}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 10.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onLikeDislike(product.id, false) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Dislike",
                            tint = if (product.dislikedBy.contains(currentUserId))
                                MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${product.dislikes}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 10.sp
                    )
                }

                IconButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(product.amazonUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(28.dp),
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Ir a la oferta",
                        tint = OrangeFF6200,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}