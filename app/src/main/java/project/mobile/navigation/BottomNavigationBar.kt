package project.mobile.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import project.mobile.ui.theme.*

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onAddProductClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = BottomNavBackground,
        contentColor = BottomNavSelected,
        modifier = Modifier
            .height(80.dp)
            .border(width = 1.dp, color = BottomNavBorder, shape = RoundedCornerShape(0.dp))
    ) {
        Box(modifier = Modifier.height(2.dp)) {
            HorizontalDivider(
                color = BottomNavDivider,
                thickness = 1.dp,
                modifier = Modifier.matchParentSize()
            )
        }

        NavigationBarItem(
            icon = { Icon(Icons.Default.Menu, contentDescription = "Menu", modifier = Modifier.size(32.dp)) },
            label = { Text("Menu") },
            selected = false,
            onClick = { /* Sin acción por ahora */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BottomNavSelected,
                selectedTextColor = BottomNavSelected,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = BottomNavIndicator
            ),
            modifier = Modifier.background(color = BottomNavIndicator, shape = RoundedCornerShape(12.dp))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "My Alerts", modifier = Modifier.size(32.dp)) },
            label = { Text("My Alerts") },
            selected = false,
            onClick = { /* Sin acción por ahora */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BottomNavSelected,
                selectedTextColor = BottomNavSelected,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = BottomNavIndicator
            ),
            modifier = Modifier.background(color = BottomNavIndicator, shape = RoundedCornerShape(12.dp))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = "Share", modifier = Modifier.size(32.dp)) },
            label = { Text("Share") },
            selected = false,
            onClick = onAddProductClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BottomNavSelected,
                selectedTextColor = BottomNavSelected,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = BottomNavIndicator
            ),
            modifier = Modifier.background(color = BottomNavIndicator, shape = RoundedCornerShape(12.dp))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Mail, contentDescription = "Inbox", modifier = Modifier.size(32.dp)) },
            label = { Text("Inbox") },
            selected = false,
            onClick = { /* Sin acción por ahora */ },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BottomNavSelected,
                selectedTextColor = BottomNavSelected,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = BottomNavIndicator
            ),
            modifier = Modifier.background(color = BottomNavIndicator, shape = RoundedCornerShape(12.dp))
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(32.dp)) },
            label = { Text("Profile") },
            selected = false,
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BottomNavSelected,
                selectedTextColor = BottomNavSelected,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = BottomNavIndicator
            ),
            modifier = Modifier.background(color = BottomNavIndicator, shape = RoundedCornerShape(12.dp))
        )
    }
}