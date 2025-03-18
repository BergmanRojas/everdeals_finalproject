package project.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import project.mobile.R
import project.mobile.controller.ProductViewModel
import project.mobile.model.Product
import project.mobile.ui.theme.*
import project.mobile.navigation.BottomNavigationBar
import project.mobile.navigation.TopNavigationBar
import project.mobile.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    onAddProductClick: () -> Unit,
    onProfileClick: () -> Unit,
    productViewModel: ProductViewModel,
    navController: NavController
) {
    val products by productViewModel.products.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> productViewModel.loadProducts() // Featured
            1 -> productViewModel.loadProductsByVotes() // Most Voted
            2 -> productViewModel.loadProductsByRising() // Rising
            3 -> productViewModel.loadProductsByDate() // New
        }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                productViewModel = productViewModel,
                navController = navController
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onAddProductClick = onAddProductClick,
                onProfileClick = onProfileClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (products.isEmpty()) {
                Text(
                    text = "No deals available yet",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
            } else {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = OrangeFF6200,
                    edgePadding = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = OrangeFF6200
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Featured") },
                        selectedContentColor = OrangeFF6200,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Most Voted") },
                        selectedContentColor = OrangeFF6200,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Rising 99+") },
                        selectedContentColor = OrangeFF6200,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("New") },
                        selectedContentColor = OrangeFF6200,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onProductClick = { productId ->
                                navController.navigate(route = Screen.ProductDetail.createRoute(productId))
                            },
                            onLikeDislike = { id, isLike ->
                                productViewModel.toggleLikeDislike(id, isLike)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onProductClick: (String) -> Unit,
    onLikeDislike: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product.id) },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen con overlay de descuento
            Box(
                modifier = Modifier
                    .size(120.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Product image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Descuento overlay
                Surface(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopEnd),
                    color = EverdealsRed.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "-${calculateDiscount(product.originalPrice, product.currentPrice)}%",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Contenido (título, precios, votos)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título y precios
                Column {
                    Text(
                        text = product.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${product.currentPrice}€",
                            color = EverdealsRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${product.originalPrice}€",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                // Info adicional (usuario, tiempo y votos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Usuario y tiempo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(product.userPhotoUrl.ifEmpty { R.drawable.default_avatar })
                                .crossfade(true)
                                .build(),
                            contentDescription = "User avatar",
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape),
                            fallback = painterResource(id = R.drawable.default_avatar),
                            error = painterResource(id = R.drawable.default_avatar)
                        )
                        Text(
                            text = "${product.userName} · ${formatTimestamp(product.createdAt)}",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Votos
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { onLikeDislike(product.id, true) },
                            modifier = Modifier.size(28.dp)
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
            }
        }
    }
}

private fun calculateDiscount(originalPrice: Double, currentPrice: Double): Int {
    return ((originalPrice - currentPrice) / originalPrice * 100).toInt()
}

private fun getStoreName(url: String): String {
    return when {
        url.contains("amazon") -> "Amazon"
        url.contains("elcorteingles") -> "El Corte Inglés"
        url.contains("aliexpress") -> "AliExpress"
        else -> "Store"
    }
}

private fun formatTimestamp(timestamp: Timestamp): String {
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
    return FirebaseAuth.getInstance().currentUser?.uid ?: ""
}