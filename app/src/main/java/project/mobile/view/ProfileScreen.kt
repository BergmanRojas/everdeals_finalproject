package project.mobile.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.mobile.components.DealCard
import project.mobile.controller.AuthManager
import project.mobile.controller.ProfileViewModel
import project.mobile.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authManager: AuthManager,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateBack: () -> Unit,
    onShareClick: () -> Unit,
    onAddProductClick: () -> Unit,
    onProfileClick: () -> Unit,
    isOwnProfile: Boolean,
    navController: NavController,
    userId: String = "" // Nuevo parámetro
) {
    val user by viewModel.userState.collectAsState()
    val error by viewModel.errorState.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val deals by viewModel.deals.collectAsState()
    val forumTopics by viewModel.forumTopics.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val fetchError by viewModel.fetchError.collectAsState()

    var selectedTab by remember { mutableStateOf(1) }

    LaunchedEffect(userId) {
        Log.d("ProfileScreen", "Loading profile for userId: $userId, isOwnProfile: $isOwnProfile")
        viewModel.loadProfileData(userId)
    }

    LaunchedEffect(user, isLoading) {
        Log.d("ProfileScreen", "User state updated: $user, isOwnProfile: $isOwnProfile, isLoading: $isLoading")
        if (user == null && !isLoading && fetchError != null) {
            Log.w("ProfileScreen", "User is null, not loading, and fetch error occurred: $fetchError")
            if (isOwnProfile) {
                onSignOut()
            }
        }
    }

    LaunchedEffect(selectedTab) {
        Log.d("ProfileScreen", "Selected tab changed to: $selectedTab")
    }

    val userName = user?.name?.takeIf { it.isNotBlank() } ?: user?.username?.takeIf { it.isNotBlank() } ?: "User not available"
    val memberSince = "Member since ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Loading...",
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            } else if (fetchError != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fetchError ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                ProfileHeader(
                    viewModel = viewModel,
                    userName = userName,
                    memberSince = memberSince,
                    onNavigateToSettings = onNavigateToSettings,
                    onShareClick = onShareClick,
                    onNavigateToDeals = { selectedTab = 1 },
                    isOwnProfile = isOwnProfile,
                    authManager = authManager,
                    onSendMessage = { message ->
                        user?.id?.let { viewModel.sendMessage(it, message) }
                    },
                    onNavigateBack = onNavigateBack,
                    navController = navController
                )
                if (isOwnProfile) {
                    ProfileTabs(
                        activities = activities,
                        deals = deals,
                        forumTopics = forumTopics,
                        stats = stats,
                        onLikeDislike = viewModel::toggleLikeDislike,
                        selectedTab = selectedTab,
                        onTabSelected = { newTab ->
                            selectedTab = newTab
                            Log.d("ProfileScreen", "Tab selected: $newTab")
                        },
                        navController = navController,
                        viewModel = viewModel // Pasamos viewModel
                    )
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 100.dp)
                    ) {
                        if (deals.isEmpty()) {
                            item {
                                Text(
                                    text = "No deals available",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )
                            }
                        } else {
                            items(deals) { product ->
                                DealCard(
                                    product = product,
                                    onLikeDislike = { productId, isLike ->
                                        viewModel.toggleLikeDislike(productId, isLike)
                                    },
                                    onClick = {
                                        navController.navigate(Screen.ProductDetail.createRoute(product.id))
                                    },
                                    onUserClick = { userId ->
                                        navController.navigate(Screen.Profile.createRoute(userId, false))
                                    }
                                )
                            }
                        }
                        item {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.primary,
                                thickness = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}