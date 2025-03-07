package project.mobile.view.navigations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import project.mobile.R

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route ?: Screen.Home.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 60.dp, // Mantenido para intentar sombra, pero complementado con borde
        modifier = Modifier
            .height(80.dp)
            .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(0.dp)) // Borde gris
    ) {
        // Contenedor para limitar el HorizontalDivider y evitar interferencia
        Box(modifier = Modifier.height(2.dp)) {
            HorizontalDivider(
                color = Color.DarkGray, // Línea divisoria sutil
                thickness = 1.dp,
                modifier = Modifier.matchParentSize()
            )
        }

        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home",
                    tint = if (route == Screen.Home.route) Color(0xFF40C4FF) else Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = route == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF40C4FF),
                unselectedIconColor = Color.Unspecified,
                indicatorColor = Color(0xFFE1F5FE)
            ),
            modifier = if (route == Screen.Home.route) Modifier
                .background(
                    color = Color(0xFFE1F5FE),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_user),
                    contentDescription = "User Info",
                    tint = if (route == Screen.UserInfo.route) Color(0xFF40C4FF) else Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = route == Screen.UserInfo.route,
            onClick = {
                navController.navigate(Screen.UserInfo.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF40C4FF),
                unselectedIconColor = Color.Unspecified,
                indicatorColor = Color(0xFFE1F5FE)
            ),
            modifier = if (route == Screen.UserInfo.route) Modifier
                .background(
                    color = Color(0xFFE1F5FE),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_deals),
                    contentDescription = "Categories",
                    tint = if (route == Screen.Categories.route) Color(0xFF40C4FF) else Color.Unspecified,
                    modifier = Modifier.size(72.dp) // Mantenido como 72.dp
                )
            },
            selected = route == Screen.Categories.route,
            onClick = {
                navController.navigate(Screen.Categories.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF40C4FF),
                unselectedIconColor = Color.Unspecified,
                indicatorColor = Color(0xFFE1F5FE)
            ),
            modifier = if (route == Screen.Categories.route) Modifier
                .background(
                    color = Color(0xFFE1F5FE),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_shopping),
                    contentDescription = "Share",
                    tint = if (route == Screen.Share.route) Color(0xFF40C4FF) else Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = route == Screen.Share.route,
            onClick = {
                navController.navigate(Screen.Share.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF40C4FF),
                unselectedIconColor = Color.Unspecified,
                indicatorColor = Color(0xFFE1F5FE)
            ),
            modifier = if (route == Screen.Share.route) Modifier
                .background(
                    color = Color(0xFFE1F5FE),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_community),
                    contentDescription = "Community",
                    tint = if (route == Screen.Community.route) Color(0xFF40C4FF) else Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            },
            selected = route == Screen.Community.route,
            onClick = {
                navController.navigate(Screen.Community.route) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF40C4FF),
                unselectedIconColor = Color.Unspecified,
                indicatorColor = Color(0xFFE1F5FE)
            ),
            modifier = if (route == Screen.Community.route) Modifier
                .background(
                    color = Color(0xFFE1F5FE),
                    shape = RoundedCornerShape(12.dp)
                ) else Modifier
        )
    }
}