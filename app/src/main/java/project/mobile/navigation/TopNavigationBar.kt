package project.mobile.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import project.mobile.controller.ProfileViewModel
import project.mobile.ui.theme.*

val PlayStationBlue = Color(0xFF00BFFF)
val PlayStationPink = Color(0xFF9046FF)

@Composable
fun TopNavigationBar(
    productViewModel: ProductViewModel,
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCouponsDialog by remember { mutableStateOf(false) }
    var showDealsDialog by remember { mutableStateOf(false) }
    var showFreeDialog by remember { mutableStateOf(false) }
    var showForumDialog by remember { mutableStateOf(false) }

    val categories by productViewModel.categories.collectAsState()
    val selectedCategory by productViewModel.selectedCategory.collectAsState()
    val unreadMessagesCount by profileViewModel.unreadMessagesCount.collectAsState()

    val Purple9046FF = Color(0xFF9046FF)

    val couponsOptions = listOf("10% Off", "20% Off", "Free Shipping", "50% Off", "Buy 1 Get 1", "Free Gift", "Special Offer", "Limited Time")
    val dealsOptions = listOf("Daily Deals", "Flash Sales", "Clearance", "Weekend Special", "Holiday Sale", "Exclusive Offer", "Last Chance", "Hot Deals")
    val freeOptions = listOf("Free Items", "Free Trials", "Giveaways", "Free Samples", "Free Shipping", "Free Returns", "Free Setup", "Free Consultation")
    val forumOptions = listOf("General", "Tech", "Deals Discussion", "Gaming", "Fashion", "Food", "Travel", "DIY")

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
            .padding(top = 40.dp, bottom = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                Box(
                    modifier = Modifier
                        .padding(start = 1.dp)
                ) {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Messages.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_messages),
                            contentDescription = "Messages",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    if (unreadMessagesCount > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(
                                    x = 2.dp,
                                    y = (-2).dp
                                ), // Ajustamos el offset para que el badge se vea bien con el nuevo espaciado
                            color = Purple9046FF.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = unreadMessagesCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
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