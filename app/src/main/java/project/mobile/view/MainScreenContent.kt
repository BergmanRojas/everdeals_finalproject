package project.mobile.view

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.mobile.components.DealCard
import project.mobile.controller.ProductViewModel
import project.mobile.navigation.Screen
import project.mobile.navigation.TopNavigationBar
import project.mobile.ui.theme.OrangeFF6200
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import kotlinx.coroutines.launch
import project.mobile.controller.AuthManager
import project.mobile.controller.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    onAddProductClick: () -> Unit,
    onProfileClick: () -> Unit,
    productViewModel: ProductViewModel,
    profileViewModel: ProfileViewModel, // Añadimos el parámetro
    navController: NavController,
    authManager: AuthManager
) {
    val products by productViewModel.products.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    var navigationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
        isLoading = false
    }

    LaunchedEffect(selectedTab) {
        isLoading = true
        when (selectedTab) {
            0 -> productViewModel.loadProducts() // Featured
            1 -> productViewModel.loadProductsByVotes() // Most Voted
            2 -> productViewModel.loadProductsByRising() // Rising
            3 -> productViewModel.loadProductsByDate() // New
        }
        isLoading = false
    }

    LaunchedEffect(navigationError) {
        if (navigationError != null) {
            Toast.makeText(context, navigationError, Toast.LENGTH_SHORT).show()
            navigationError = null
        }
    }

    Scaffold(
        topBar = {
            TopNavigationBar(
                productViewModel = productViewModel,
                navController = navController,
                profileViewModel = profileViewModel // Pasamos el parámetro
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.navigationBars.asPaddingValues())
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = OrangeFF6200,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
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

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeFF6200)
                }
            } else if (products.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (selectedTab) {
                            0 -> "No featured deals available"
                            1 -> "No most voted deals available"
                            2 -> "No rising deals available"
                            3 -> "No new deals available"
                            else -> "No deals available yet"
                        },
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 0.dp,
                        bottom = 100.dp
                    )
                ) {
                    items(products) { product ->
                        DealCard(
                            product = product,
                            onLikeDislike = { productId, isLike ->
                                productViewModel.toggleLikeDislike(productId, isLike)
                            },
                            onClick = {
                                navController.navigate(Screen.ProductDetail.createRoute(product.id))
                            },
                            onUserClick = { userId ->
                                try {
                                    Log.d("MainScreen", "Attempting to navigate to profile for userId: $userId")
                                    navController.navigate(Screen.Profile.createRoute(userId, false))
                                    Log.d("MainScreen", "Navigation executed successfully")
                                } catch (e: Exception) {
                                    Log.e("MainScreen", "Navigation failed: ${e.message}", e)
                                    navigationError = "Error al navegar al perfil"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}