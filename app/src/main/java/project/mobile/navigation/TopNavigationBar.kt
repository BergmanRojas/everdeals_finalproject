package project.mobile.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.mobile.R
import project.mobile.controller.ProductViewModel
import project.mobile.ui.theme.*

// Colores para el gradiente del diálogo
val PlayStationBlue = Color(0xFF00BFFF) // Azul claro
val PlayStationPink = Color(0xFF9046FF) // Rosa claro

@Composable
fun TopNavigationBar(
    productViewModel: ProductViewModel,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCouponsDialog by remember { mutableStateOf(false) }
    var showDealsDialog by remember { mutableStateOf(false) }
    var showFreeDialog by remember { mutableStateOf(false) }
    var showForumDialog by remember { mutableStateOf(false) }

    val categories by productViewModel.categories.collectAsState()
    val selectedCategory by productViewModel.selectedCategory.collectAsState()

    // Listas estáticas para los otros menús (reemplazar con datos dinámicos si los tienes)
    val couponsOptions = listOf("10% Off", "20% Off", "Free Shipping", "50% Off", "Buy 1 Get 1", "Free Gift", "Special Offer", "Limited Time")
    val dealsOptions = listOf("Daily Deals", "Flash Sales", "Clearance", "Weekend Special", "Holiday Sale", "Exclusive Offer", "Last Chance", "Hot Deals")
    val freeOptions = listOf("Free Items", "Free Trials", "Giveaways", "Free Samples", "Free Shipping", "Free Returns", "Free Setup", "Free Consultation")
    val forumOptions = listOf("General", "Tech", "Deals Discussion", "Gaming", "Fashion", "Food", "Travel", "DIY")

    // Estados para las selecciones (puedes moverlos a ProductViewModel si lo prefieres)
    var selectedCoupon by remember { mutableStateOf<String?>(null) }
    var selectedDeal by remember { mutableStateOf<String?>(null) }
    var selectedFree by remember { mutableStateOf<String?>(null) }
    var selectedForum by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(EverdealsYellow, EverdealsRed)
                )
            )
            .padding(top = 40.dp, bottom = 0.dp) // Solo padding vertical
    ) {
        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth() // Sin padding horizontal
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.everdeals),
                    contentDescription = "EverDeals Logo",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 8.dp)
                )

                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        productViewModel.searchProducts(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                    placeholder = {
                        Text(
                            "Search deals...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = OrangeFF6200,
                            modifier = Modifier.size(22.dp)
                        )
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )

                IconButton(
                    onClick = {
                        navController.navigate("add_product") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .drawBehind {
                            drawCircle(
                                color = CircleBackground,
                                radius = 12.dp.toPx(),
                                center = Offset(center.x, center.y)
                            )
                        }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = "Share",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
        }

        // Horizontal scrollable buttons sin padding horizontal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 0.dp), // Solo padding vertical
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Categories
            TextButton(
                onClick = { showCategoryDialog = true },
                modifier = Modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_categories),
                        contentDescription = "Categories",
                        tint = if (selectedCategory != null) OrangeFF6200 else Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCategory ?: "Categories",
                        color = if (selectedCategory != null) OrangeFF6200 else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (selectedCategory != null) OrangeFF6200 else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Coupons
            TextButton(
                onClick = { showCouponsDialog = true },
                modifier = Modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_coupons),
                        contentDescription = "Coupons",
                        tint = if (selectedCoupon != null) OrangeFF6200 else Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCoupon ?: "Coupons",
                        color = if (selectedCoupon != null) OrangeFF6200 else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (selectedCoupon != null) OrangeFF6200 else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Deals
            TextButton(
                onClick = { showDealsDialog = true },
                modifier = Modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_deals),
                        contentDescription = "Deals",
                        tint = if (selectedDeal != null) OrangeFF6200 else Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedDeal ?: "Deals",
                        color = if (selectedDeal != null) OrangeFF6200 else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (selectedDeal != null) OrangeFF6200 else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Free
            TextButton(
                onClick = { showFreeDialog = true },
                modifier = Modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_free),
                        contentDescription = "Free",
                        tint = if (selectedFree != null) OrangeFF6200 else Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedFree ?: "Free",
                        color = if (selectedFree != null) OrangeFF6200 else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (selectedFree != null) OrangeFF6200 else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Forum
            TextButton(
                onClick = { showForumDialog = true },
                modifier = Modifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_forum),
                        contentDescription = "Forum",
                        tint = if (selectedForum != null) OrangeFF6200 else Color.Unspecified,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedForum ?: "Forum",
                        color = if (selectedForum != null) OrangeFF6200 else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (selectedForum != null) OrangeFF6200 else Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Diálogos de selección (sin cambios)
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select category") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                productViewModel.setSelectedCategory(null)
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "All categories",
                                color = if (selectedCategory == null) OrangeFF6200 else Color.White
                            )
                        }
                    }
                    items(categories) { category ->
                        TextButton(
                            onClick = {
                                productViewModel.setSelectedCategory(category)
                                showCategoryDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                category,
                                color = if (category == selectedCategory) OrangeFF6200 else Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PlayStationBlue, PlayStationPink),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    // Coupons selection dialog
    if (showCouponsDialog) {
        AlertDialog(
            onDismissRequest = { showCouponsDialog = false },
            title = { Text("Select coupon") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                selectedCoupon = null
                                showCouponsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "All coupons",
                                color = if (selectedCoupon == null) OrangeFF6200 else Color.White
                            )
                        }
                    }
                    items(couponsOptions) { coupon ->
                        TextButton(
                            onClick = {
                                selectedCoupon = coupon
                                showCouponsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                coupon,
                                color = if (coupon == selectedCoupon) OrangeFF6200 else Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PlayStationBlue, PlayStationPink),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    // Deals selection dialog
    if (showDealsDialog) {
        AlertDialog(
            onDismissRequest = { showDealsDialog = false },
            title = { Text("Select deal type") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                selectedDeal = null
                                showDealsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "All deals",
                                color = if (selectedDeal == null) OrangeFF6200 else Color.White
                            )
                        }
                    }
                    items(dealsOptions) { deal ->
                        TextButton(
                            onClick = {
                                selectedDeal = deal
                                showDealsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                deal,
                                color = if (deal == selectedDeal) OrangeFF6200 else Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PlayStationBlue, PlayStationPink),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    // Free selection dialog
    if (showFreeDialog) {
        AlertDialog(
            onDismissRequest = { showFreeDialog = false },
            title = { Text("Select free option") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                selectedFree = null
                                showFreeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "All free options",
                                color = if (selectedFree == null) OrangeFF6200 else Color.White
                            )
                        }
                    }
                    items(freeOptions) { free ->
                        TextButton(
                            onClick = {
                                selectedFree = free
                                showFreeDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                free,
                                color = if (free == selectedFree) OrangeFF6200 else Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PlayStationBlue, PlayStationPink),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    // Forum selection dialog
    if (showForumDialog) {
        AlertDialog(
            onDismissRequest = { showForumDialog = false },
            title = { Text("Select forum topic") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                selectedForum = null
                                showForumDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "All topics",
                                color = if (selectedForum == null) OrangeFF6200 else Color.White
                            )
                        }
                    }
                    items(forumOptions) { topic ->
                        TextButton(
                            onClick = {
                                selectedForum = topic
                                showForumDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                topic,
                                color = if (topic == selectedForum) OrangeFF6200 else Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PlayStationBlue, PlayStationPink),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}