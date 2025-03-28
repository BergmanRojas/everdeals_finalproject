package project.mobile.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import project.mobile.controller.AuthManager
import project.mobile.controller.MessagingViewModel
import project.mobile.controller.ProfileViewModel
import project.mobile.model.User
import project.mobile.ui.theme.EverDealsTheme
import project.mobile.ui.theme.Purple9046FF
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    targetUserId: String,
    targetUserName: String,
    messagingViewModel: MessagingViewModel,
    profileViewModel: ProfileViewModel,
    authManager: AuthManager,
    onNavigateBack: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val messages by messagingViewModel.messages.collectAsState()
    val error by messagingViewModel.errorState.collectAsState()
    var currentUser by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        currentUser = authManager.getCurrentUser()
        Log.d("ChatScreen", "Current user: ${currentUser?.id}")
        messagingViewModel.loadMessagesWithUser(targetUserId)
        messagingViewModel.setupMessagesListenerForChat(targetUserId)
    }

    LaunchedEffect(messages) {
        Log.d("ChatScreen", "Messages updated: ${messages.size} messages")
        messages.forEach { message ->
            Log.d("ChatScreen", "Message: $message")
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            Log.e("ChatScreen", "Error: $error")
        }
    }

    fun sendMessage() {
        if (messageText.isNotBlank()) {
            Log.d("ChatScreen", "Sending message to $targetUserId: $messageText")
            messagingViewModel.sendMessage(targetUserId, messageText)
            messageText = ""
        }
    }

    EverDealsTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = profileViewModel.userState.value?.photoUrl ?: "",
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Chat with $targetUserName")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Type a message") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = Purple9046FF,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = Purple9046FF,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    IconButton(
                        onClick = { sendMessage() },
                        enabled = messageText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank()) Purple9046FF else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
            ) {
                if (error != null) {
                    Text(
                        text = "Failed to load messages: $error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(messages) { message ->
                            val isSentByCurrentUser = message.senderId == currentUser?.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSentByCurrentUser) Purple9046FF else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        Text(
                                            text = message.content,
                                            color = if (isSentByCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = message.timestamp.toDate()?.let {
                                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                                            } ?: "",
                                            color = if (isSentByCurrentUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}