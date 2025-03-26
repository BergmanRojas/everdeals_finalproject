package project.mobile.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import project.mobile.R
import project.mobile.controller.AuthManager
import project.mobile.controller.ProfileViewModel

@Composable
fun ProfileHeader(
    viewModel: ProfileViewModel,
    userName: String,
    memberSince: String,
    onNavigateToSettings: () -> Unit,
    onShareClick: () -> Unit,
    onNavigateToDeals: () -> Unit,
    isOwnProfile: Boolean,
    authManager: AuthManager,
    onSendMessage: (String) -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavController // Añadimos navController
) {
    val user by viewModel.userState.collectAsState()
    val deals by viewModel.deals.collectAsState()
    val followersWithNames by viewModel.followersWithNames.collectAsState()
    val followingWithNames by viewModel.followingWithNames.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showFollowersDialog by remember { mutableStateOf(false) }
    var showFollowingDialog by remember { mutableStateOf(false) }
    var isFollowing by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        val currentUser = authManager.getCurrentUser()
        isFollowing = user?.followers?.contains(currentUser?.id) ?: false
    }

    val profileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { viewModel.updateProfilePhoto(it.toString()) } }
    }
    val backgroundLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { viewModel.updateBackgroundPhoto(it.toString()) } }
    }

    val shareProfile = {
        val shareText = "Check out my profile on EverDeals: $userName"
        ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setText(shareText)
            .startChooser()
        onShareClick()
    }

    Box {
        AsyncImage(
            model = user?.backgroundUrl ?: R.drawable.background_user,
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )

        if (!isOwnProfile) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isOwnProfile) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { backgroundLauncher.launch("image/*") }) {
                    Icon(
                        Icons.Default.Edit,
                        "Change Background",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.MoreVert,
                        "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 2.dp)
        ) {
            if (user?.photoUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(enabled = isOwnProfile) { profileLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "?",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 40.sp
                    )
                }
            } else {
                AsyncImage(
                    model = user?.photoUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(enabled = isOwnProfile) { profileLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            }
            if (isOwnProfile) {
                Icon(
                    Icons.Default.CameraAlt,
                    "Change Profile Photo",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .offset(x = 8.dp, y = 8.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                        .clickable { profileLauncher.launch("image/*") },
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 6.dp, bottom = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isOwnProfile) {
                Button(
                    onClick = shareProfile,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.onSecondary)
                    Spacer(Modifier.width(4.dp))
                    Text("SHARE", color = MaterialTheme.colorScheme.onSecondary)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.toggleFollow(user?.id ?: "")
                        isFollowing = !isFollowing
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSecondary
                    )
                }
                Button(
                    onClick = { showMessageDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Message",
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        Text(
            userName,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (isOwnProfile) {
            Text(
                memberSince,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        Row(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "${deals.size} Posts",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { onNavigateToDeals() }
            )
            Text(
                "${followersWithNames.size} Followers",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { if (isOwnProfile) showFollowersDialog = true }
            )
            Text(
                "${followingWithNames.size} Following",
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.clickable { if (isOwnProfile) showFollowingDialog = true }
            )
        }
    }

    if (isOwnProfile && showFollowersDialog) {
        AlertDialog(
            onDismissRequest = { showFollowersDialog = false },
            title = { Text("Followers") },
            text = {
                LazyColumn {
                    items(followersWithNames) { (id, name) ->
                        Text(
                            text = name,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate("profile/$id/false")
                                    showFollowersDialog = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFollowersDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (isOwnProfile && showFollowingDialog) {
        AlertDialog(
            onDismissRequest = { showFollowingDialog = false },
            title = { Text("Following") },
            text = {
                LazyColumn {
                    items(followingWithNames) { (id, name) ->
                        Text(
                            text = name,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable {
                                    navController.navigate("profile/$id/false")
                                    showFollowingDialog = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFollowingDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (!isOwnProfile && showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Send Message to $userName") },
            text = {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSendMessage(messageText)
                        messageText = ""
                        showMessageDialog = false
                    },
                    enabled = messageText.isNotBlank()
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}