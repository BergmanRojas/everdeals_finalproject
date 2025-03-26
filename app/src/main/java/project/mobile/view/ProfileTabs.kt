package project.mobile.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import project.mobile.components.ActivityCard
import project.mobile.components.DealCard
import project.mobile.model.Product
import project.mobile.model.UserActivity
import project.mobile.navigation.Screen

@Composable
fun ProfileTabs(
    activities: List<UserActivity>,
    deals: List<Product>,
    forumTopics: List<String>,
    stats: List<String>,
    onLikeDislike: (String, Boolean) -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavController
) {
    val tabs = listOf("Activities", "Deals", "Forum", "Statistics")

    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        tabs.forEachIndexed { index, tabName ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(tabName) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    when (selectedTab) {
        0 -> { // Activities
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 100.dp)
            ) {
                if (activities.isEmpty()) {
                    item {
                        Text(
                            text = "No activities yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(activities) { activity ->
                        ActivityCard(activity = activity)
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
        1 -> { // Deals
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
                            onLikeDislike = onLikeDislike,
                            onClick = {
                                navController.navigate(Screen.ProductDetail.createRoute(product.id))
                            },
                            onUserClick = { userId ->
                                navController.navigate(Screen.Profile.route)
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
        2 -> { // Forum
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 100.dp)
            ) {
                if (forumTopics.isEmpty()) {
                    item {
                        Text(
                            text = "No forum topics available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(forumTopics) { topic ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = topic,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
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
        3 -> { // Statistics
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 100.dp)
            ) {
                if (stats.isEmpty()) {
                    item {
                        Text(
                            text = "No statistics available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(stats) { stat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stat,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )
                            }
                        }
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
    }
}