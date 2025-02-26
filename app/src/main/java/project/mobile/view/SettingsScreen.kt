package project.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import project.mobile.model.User
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    currentUser: User?
) {
    var user by remember { mutableStateOf(currentUser) }

    LaunchedEffect(currentUser) {
        user = currentUser
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        // User Profile Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9D4EDD)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.photoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = user?.photoUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = user?.name?.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // User Info
                Column {
                    Text(
                        text = user?.name ?: "Unknown",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user?.email ?: "",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "@${user?.username ?: ""}",
                        color = Color(0xFF03A9F4),
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: Implement edit profile */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }
        }

        HorizontalDivider(color = Color(0xFF2A2A2A))

        // Settings Items
        SettingsItem(
            title = "Preferences",
            icon = Icons.Default.Settings,
            onClick = { /* TODO */ }
        )
        SettingsItem(
            title = "Social Connection",
            icon = Icons.Default.Share,
            onClick = { /* TODO */ }
        )
        SettingsItem(
            title = "Notifications",
            icon = Icons.Default.Notifications,
            onClick = { /* TODO */ }
        )
        SettingsItem(
            title = "Subscriptions",
            icon = Icons.Default.Mail,
            onClick = { /* TODO */ }
        )
        SettingsItem(
            title = "Following / Ignored",
            icon = Icons.Default.Person,
            onClick = { /* TODO */ }
        )

        HorizontalDivider(color = Color(0xFF2A2A2A))

        // Avatar Change Section
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Change Avatar",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* TODO: Implement replace avatar */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    Text("Replace")
                }
                TextButton(
                    onClick = { /* TODO: Implement remove avatar */ },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF5722))
                ) {
                    Text("Remove")
                }
            }

            Text(
                text = "For optimal results use a square image",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

