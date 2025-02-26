package project.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import project.mobile.controller.ProductViewModel
import project.mobile.model.Product
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import com.google.firebase.auth.FirebaseAuth
import project.mobile.R
import com.google.firebase.Timestamp
import androidx.compose.foundation.Image
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.res.painterResource

val EverdealsBackground = Color(0xFF1A1A1A)
val EverdealsSurface = Color(0xFF2A2A2A)
val EverdealsGreen = Color(0xFF00C853)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    onAddProductClick: () -> Unit,
    onProfileClick: () -> Unit,
    productViewModel: ProductViewModel,
    navController: androidx.navigation.NavController
) {
    val products by productViewModel.products.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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
            Column {
                // Top navigation bar
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (showSearch) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        productViewModel.searchProducts(it)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    placeholder = { Text("Search deals...", color = Color.Gray) },
                                    singleLine = true,
                                    leadingIcon = {
                                        IconButton(onClick = { showSearch = false }) {
                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = "Back",
                                                tint = EverdealsGreen
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = {
                                                searchQuery = ""
                                                productViewModel.searchProducts("")
                                            }) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Clear search",
                                                    tint = EverdealsGreen
                                                )
                                            }
                                        }
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = EverdealsSurface,
                                        unfocusedContainerColor = EverdealsSurface,
                                        disabledContainerColor = EverdealsSurface,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = EverdealsGreen,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = EverdealsGreen
                                    )
                                )
                            } else {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.everdeals),
                                        contentDescription = "EverDeals Logo",
                                        modifier = Modifier
                                            .height(40.dp)
                                            .padding(end = 8.dp)
                                    )
                                }
                                IconButton(onClick = { showSearch = true }) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = EverdealsGreen
                                    )
                                }
                            }
                        }
                    },
                    actions = { },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = EverdealsBackground
                    )
                )

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = EverdealsBackground,
                    contentColor = EverdealsGreen,
                    edgePadding = 0.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = EverdealsGreen
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Featured") },
                        selectedContentColor = EverdealsGreen,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Most Voted") },
                        selectedContentColor = EverdealsGreen,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Rising 99+") },
                        selectedContentColor = EverdealsGreen,
                        unselectedContentColor = Color.Gray
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("New") },
                        selectedContentColor = EverdealsGreen,
                        unselectedContentColor = Color.Gray
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = EverdealsBackground,
                contentColor = EverdealsGreen
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                    label = { Text("Menu") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EverdealsGreen,
                        selectedTextColor = EverdealsGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = EverdealsSurface
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "My Alerts") },
                    label = { Text("My Alerts") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EverdealsGreen,
                        selectedTextColor = EverdealsGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = EverdealsSurface
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Share") },
                    label = { Text("Share") },
                    selected = false,
                    onClick = onAddProductClick,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EverdealsGreen,
                        selectedTextColor = EverdealsGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = EverdealsSurface
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Mail, contentDescription = "Inbox") },
                    label = { Text("Inbox") },
                    selected = false,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EverdealsGreen,
                        selectedTextColor = EverdealsGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = EverdealsSurface
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onProfileClick,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EverdealsGreen,
                        selectedTextColor = EverdealsGreen,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = EverdealsSurface
                    )
                )
            }
        }
    ) { paddingValues ->
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No deals available yet",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(products) { product ->
                    DealCard(
                        product = product,
                        onLikeDislike = { productId, isLike ->
                            productViewModel.toggleLikeDislike(productId, isLike)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DealCard(
    product: Product,
    onLikeDislike: (String, Boolean) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onLikeDislike(product.id, false) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dislike",
                            tint = if (product.dislikedBy.contains(getCurrentUserId())) Color(0xFFFF5722) else Color.Gray
                        )
                    }
                    Text(
                        text = "${product.likes - product.dislikes}°",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = { onLikeDislike(product.id, true) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropUp,
                            contentDescription = "Like",
                            tint = if (product.likedBy.contains(getCurrentUserId())) Color(0xFF00C853) else Color.Gray
                        )
                    }
                }
                Text(
                    text = formatTimestamp(product.createdAt),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Product image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Text(
                        text = product.name,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${product.currentPrice}€",
                            color = Color(0xFFFF5722),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${product.originalPrice}€",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = "-${calculateDiscount(product.originalPrice, product.currentPrice)}%",
                            color = Color(0xFF00C853),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Available at ${getStoreName(product.amazonUrl)}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "0",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { /* TODO: Implement deal action */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5722)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Go to deal")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User information at the bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(product.userPhotoUrl.ifEmpty { R.drawable.default_avatar })
                        .crossfade(true)
                        .build(),
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape),
                    fallback = painterResource(id = R.drawable.default_avatar),
                    error = painterResource(id = R.drawable.default_avatar)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Posted by ",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = product.userName.ifEmpty { "Anonymous" },
                    color = Color(0xFFFF5722),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
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
        diffInMillis < 60000 -> "Just now" // less than 1 minute
        diffInMillis < 3600000 -> "${diffInMillis / 60000}m ago" // less than 1 hour
        diffInMillis < 86400000 -> "${diffInMillis / 3600000}h ago" // less than 1 day
        else -> {
            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp.seconds * 1000))
        }
    }
}

private fun getCurrentUserId(): String {
    // Replace this with actual implementation to get current user ID
    return FirebaseAuth.getInstance().currentUser?.uid ?: ""
}

