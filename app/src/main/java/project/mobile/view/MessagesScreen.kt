package project.mobile.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import project.mobile.controller.AuthManager
import project.mobile.controller.MessagingViewModel
import project.mobile.model.Conversation
import project.mobile.model.Message
import project.mobile.navigation.Screen
import project.mobile.ui.theme.EverDealsTheme
import project.mobile.ui.theme.Purple9046FF

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    onNavigateBack: () -> Unit,
    navController: NavController,
    authManager: AuthManager,
    viewModel: MessagingViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val conversations by viewModel.conversations.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val error by viewModel.errorState.collectAsState()
    var currentUserId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = authManager.getCurrentUser()
        currentUserId = user?.id ?: ""
        viewModel.loadConversations()
        viewModel.setupMessagesListener()
    }

    LaunchedEffect(conversations) {
        Log.d("MessagesScreen", "Conversations updated: ${conversations.size} conversations")
        conversations.forEach { conversation ->
            Log.d("MessagesScreen", "Conversation: $conversation")
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Log.e("MessagesScreen", "Error: $error")
        }
    }

    val filteredConversations = conversations.filter {
        it.username.contains(searchQuery, ignoreCase = true) ||
                it.handle.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    EverDealsTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Messages") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: Implement settings */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.NewChat.route) },
                    containerColor = Color(0xFF1DA1F2),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.MailOutline, contentDescription = "New Message")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search Direct Messages") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (error != null) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (filteredConversations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No conversations yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredConversations) { conversation ->
                            ConversationItem(
                                conversation = conversation,
                                messages = messages,
                                currentUserId = currentUserId,
                                onClick = {
                                    navController.navigate("chat/${conversation.userId}/${conversation.username}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    messages: List<Message>,
    currentUserId: String,
    onClick: () -> Unit
) {
    val hasUnread = messages.any { message ->
        message.receiverId == currentUserId && message.senderId == conversation.userId && !message.isRead
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = conversation.profileImageUrl,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.username,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasUnread) Purple9046FF else MaterialTheme.colorScheme.onBackground
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.date,
                        fontSize = 12.sp,
                        color = if (hasUnread) Purple9046FF else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (hasUnread) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Purple9046FF)
                        )
                    }
                }
            }
            Text(
                text = conversation.handle,
                fontSize = 14.sp,
                color = if (hasUnread) Purple9046FF else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = conversation.lastMessage,
                fontSize = 14.sp,
                color = if (hasUnread) Purple9046FF else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}