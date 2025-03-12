package project.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    val items = listOf(
        ProfileMenuItem("Received Alerts", Icons.Default.Notifications) {},
        ProfileMenuItem("Saved Deals", Icons.Default.Bookmark) {},
        ProfileMenuItem("Activity", Icons.Default.Timeline) {},
        ProfileMenuItem("My Offers", Icons.Default.LocalOffer) {},
        ProfileMenuItem("My Topics", Icons.Default.Forum) {},
        ProfileMenuItem("Medals", Icons.Default.Star) {},
        ProfileMenuItem("Statistics", Icons.Default.BarChart) {},
        ProfileMenuItem("Settings", Icons.Default.Settings) { onNavigateToSettings() },
        ProfileMenuItem("Sign Out", Icons.Default.ExitToApp) { onSignOut() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        TopAppBar(
            title = { Text("Profile") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White
            )
        )

        items.forEach { item ->
            ProfileMenuItemRow(item)
            Divider(color = Color(0xFF2A2A2A))
        }
    }
}

@Composable
private fun ProfileMenuItemRow(item: ProfileMenuItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.title,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

private data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

