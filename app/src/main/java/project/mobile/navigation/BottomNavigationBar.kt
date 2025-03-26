package project.mobile.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import project.mobile.R
import project.mobile.ui.theme.BottomNavBackground
import project.mobile.ui.theme.Dark161C2A
import project.mobile.ui.theme.OrangeFF6200

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onAddProductClick: () -> Unit,
    onProfileClick: () -> Unit,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val borderColor = if (isSystemInDarkTheme()) Color(0xFFCCCCCC) else Color(0xFF666666)

    NavigationBar(
        containerColor = if (isSystemInDarkTheme()) Dark161C2A else BottomNavBackground,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2.dp.toPx()
                )
            }
    ) {
        NavigationBarItem(
            selected = selectedItem == "Menu",
            onClick = {
                navController.navigate("main") {
                    popUpTo("main") { inclusive = false }
                    launchSingleTop = true
                }
                onItemSelected("Menu")
            },
            icon = {
                Box(
                    modifier = if (selectedItem == "Menu") Modifier
                        .background(
                            color = OrangeFF6200.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                    else Modifier
                        .padding(6.dp)
                ) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = "Menu",
                        modifier = Modifier.size(34.dp),
                        tint = if (selectedItem == "Menu") Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            label = {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedItem == "Menu") OrangeFF6200 else MaterialTheme.colorScheme.onBackground
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = OrangeFF6200,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == "My Alerts",
            onClick = {
                onItemSelected("My Alerts")
                // TODO: Añadir navegación si "My Alerts" tiene una ruta específica
            },
            icon = {
                Box(
                    modifier = if (selectedItem == "My Alerts") Modifier
                        .background(
                            color = OrangeFF6200.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                    else Modifier
                        .padding(6.dp)
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "My Alerts",
                        modifier = Modifier.size(34.dp),
                        tint = if (selectedItem == "My Alerts") Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            label = {
                Text(
                    text = "My Alerts",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedItem == "My Alerts") OrangeFF6200 else MaterialTheme.colorScheme.onBackground
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = OrangeFF6200,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == "Share",
            onClick = {
                onItemSelected("Share")
                onAddProductClick()
            },
            icon = {
                Box(
                    modifier = if (selectedItem == "Share") Modifier
                        .background(
                            color = OrangeFF6200.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                    else Modifier
                        .padding(6.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Share",
                        modifier = Modifier.size(34.dp),
                        tint = if (selectedItem == "Share") Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            label = {
                Text(
                    text = "Share",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedItem == "Share") OrangeFF6200 else MaterialTheme.colorScheme.onBackground
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = OrangeFF6200,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == "Affiliate",
            onClick = {
                onItemSelected("Affiliate")
                navController.navigate(Screen.Affiliate.route) {
                    popUpTo(Screen.Main.route) { inclusive = false }
                    launchSingleTop = true
                }
            },
            icon = {
                Box(
                    modifier = if (selectedItem == "Affiliate") Modifier
                        .background(
                            color = OrangeFF6200.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                    else Modifier
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_affiliate),
                        contentDescription = "Affiliate",
                        modifier = Modifier.size(34.dp),
                        tint = if (selectedItem == "Affiliate") Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            label = {
                Text(
                    text = "Affiliate",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedItem == "Affiliate") OrangeFF6200 else MaterialTheme.colorScheme.onBackground
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = OrangeFF6200,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = selectedItem == "Profile",
            onClick = {
                onItemSelected("Profile")
                onProfileClick()
            },
            icon = {
                Box(
                    modifier = if (selectedItem == "Profile") Modifier
                        .background(
                            color = OrangeFF6200.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(6.dp)
                    else Modifier
                        .padding(6.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(34.dp),
                        tint = if (selectedItem == "Profile") Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            label = {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedItem == "Profile") OrangeFF6200 else MaterialTheme.colorScheme.onBackground
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = OrangeFF6200,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
    }
}