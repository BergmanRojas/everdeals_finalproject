package project.mobile.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import project.mobile.model.Conversation
import project.mobile.model.Message
import project.mobile.model.User
import java.text.SimpleDateFormat
import java.util.Locale

class MessagingViewModel(
    private val authManager: AuthManager
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _unreadMessagesCount = MutableStateFlow(0)
    val unreadMessagesCount: StateFlow<Int> = _unreadMessagesCount

    private var sentMessagesListener: ListenerRegistration? = null
    private var receivedMessagesListener: ListenerRegistration? = null
    private var chatMessagesListener: ListenerRegistration? = null

    fun sendMessage(targetUserId: String, content: String) {
        viewModelScope.launch {
            val currentUser = authManager.getCurrentUser() ?: return@launch
            val message = Message(
                id = firestore.collection("messages").document().id,
                senderId = currentUser.id,
                receiverId = targetUserId,
                content = content,
                timestamp = Timestamp.now(),
                isRead = false
            )
            try {
                Log.d("MessagingViewModel", "Attempting to send message: $message")
                firestore.collection("messages")
                    .document(message.id)
                    .set(message)
                    .await()
                Log.d("MessagingViewModel", "Message sent successfully: $message")
                _messages.value = _messages.value + message
            } catch (e: Exception) {
                Log.e("MessagingViewModel", "Error sending message: ${e.message}", e)
                _errorState.value = "Error sending message: ${e.message}"
            }
        }
    }

    fun loadMessagesWithUser(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            try {
                Log.d("MessagingViewModel", "Loading messages between $currentUserId and $targetUserId")
                val messagesSnapshot = firestore.collection("messages")
                    .whereIn("senderId", listOf(currentUserId, targetUserId))
                    .whereIn("receiverId", listOf(currentUserId, targetUserId))
                    .orderBy("timestamp")
                    .get()
                    .await()
                val messages = messagesSnapshot.documents.mapNotNull { doc ->
                    val message = doc.toObject<Message>()
                    message?.copy(
                        id = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                        isRead = doc.getBoolean("read") ?: false // Cambiado a "read"
                    )
                }
                Log.d("MessagingViewModel", "Loaded ${messages.size} messages: $messages")
                _messages.value = messages

                // Mark messages as read
                messages.filter { it.receiverId == currentUserId && !it.isRead }.forEach { message ->
                    try {
                        firestore.collection("messages")
                            .document(message.id)
                            .update("read", true) // Cambiado a "read"
                            .await()
                        Log.d("MessagingViewModel", "Marked message as read: ${message.id}")
                    } catch (e: Exception) {
                        Log.e("MessagingViewModel", "Error marking message as read: ${e.message}", e)
                        _errorState.value = "Error marking message as read: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                Log.e("MessagingViewModel", "Error loading messages: ${e.message}", e)
                _errorState.value = "Error loading messages: ${e.message}"
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            try {
                Log.d("MessagingViewModel", "Loading conversations for user: $currentUserId")

                val sentMessagesSnapshot = firestore.collection("messages")
                    .whereEqualTo("senderId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val receivedMessagesSnapshot = firestore.collection("messages")
                    .whereEqualTo("receiverId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val messages = (sentMessagesSnapshot.documents + receivedMessagesSnapshot.documents)
                    .mapNotNull { doc ->
                        val message = doc.toObject<Message>()
                        message?.copy(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            receiverId = doc.getString("receiverId") ?: "",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                            isRead = doc.getBoolean("read") ?: false // Cambiado a "read"
                        )
                    }
                    .distinctBy { it.id }
                    .sortedByDescending { it.timestamp.toDate().time }

                Log.d("MessagingViewModel", "Loaded ${messages.size} messages for conversations: $messages")

                val conversationMap = mutableMapOf<String, MutableList<Message>>()
                messages.forEach { message ->
                    val otherUserId = if (message.senderId == currentUserId) message.receiverId else message.senderId
                    val conversationId = if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
                    val conversationMessages = conversationMap.getOrPut(conversationId) { mutableListOf() }
                    conversationMessages.add(message)
                }

                val conversations = conversationMap.map { (conversationId, messages) ->
                    val otherUserId = messages.first().let {
                        if (it.senderId == currentUserId) it.receiverId else it.senderId
                    }
                    val userSnapshot = firestore.collection("users").document(otherUserId).get().await()
                    val user = userSnapshot.toObject<User>()
                    val lastMessage = messages.maxByOrNull { it.timestamp }!!
                    Conversation(
                        userId = otherUserId,
                        username = user?.name ?: user?.username ?: "Unknown",
                        handle = "@${user?.username ?: "unknown"}",
                        lastMessage = lastMessage.content,
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(lastMessage.timestamp.toDate()),
                        profileImageUrl = user?.photoUrl ?: ""
                    )
                }.sortedByDescending { it.date }

                Log.d("MessagingViewModel", "Loaded ${conversations.size} conversations: $conversations")
                _conversations.value = conversations

                val unreadCount = messages.count { it.receiverId == currentUserId && !it.isRead }
                Log.d("MessagingViewModel", "Unread messages count: $unreadCount")
                _unreadMessagesCount.value = unreadCount
            } catch (e: Exception) {
                Log.e("MessagingViewModel", "Error loading conversations: ${e.message}", e)
                _errorState.value = "Error loading conversations: ${e.message}"
            }
        }
    }

    fun setupMessagesListener() {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            Log.d("MessagingViewModel", "Setting up messages listener for user: $currentUserId")

            sentMessagesListener = firestore.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MessagingViewModel", "Error listening to sent messages: ${error.message}", error)
                        _errorState.value = "Error listening to sent messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("MessagingViewModel", "Sent messages snapshot is null")
                        return@addSnapshotListener
                    }

                    viewModelScope.launch {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            val message = doc.toObject<Message>()
                            message?.copy(
                                id = doc.id,
                                senderId = doc.getString("senderId") ?: "",
                                receiverId = doc.getString("receiverId") ?: "",
                                content = doc.getString("content") ?: "",
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                                isRead = doc.getBoolean("read") ?: false // Cambiado a "read"
                            )
                        }

                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex = currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedByDescending { it.timestamp.toDate().time }

                        Log.d("MessagingViewModel", "Sent messages snapshot received, ${allMessages.size} messages: $allMessages")

                        val conversationMap = mutableMapOf<String, MutableList<Message>>()
                        allMessages.forEach { message ->
                            val otherUserId = if (message.senderId == currentUserId) message.receiverId else message.senderId
                            val conversationId = if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
                            val conversationMessages = conversationMap.getOrPut(conversationId) { mutableListOf() }
                            conversationMessages.add(message)
                        }

                        val conversations = conversationMap.map { (conversationId, messages) ->
                            val otherUserId = messages.first().let {
                                if (it.senderId == currentUserId) it.receiverId else it.senderId
                            }
                            val userSnapshot = firestore.collection("users").document(otherUserId).get().await()
                            val user = userSnapshot.toObject<User>()
                            val lastMessage = messages.maxByOrNull { it.timestamp }!!
                            Conversation(
                                userId = otherUserId,
                                username = user?.name ?: user?.username ?: "Unknown",
                                handle = "@${user?.username ?: "unknown"}",
                                lastMessage = lastMessage.content,
                                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(lastMessage.timestamp.toDate()),
                                profileImageUrl = user?.photoUrl ?: ""
                            )
                        }.sortedByDescending { it.date }

                        Log.d("MessagingViewModel", "Updated ${conversations.size} conversations: $conversations")
                        _conversations.value = conversations

                        val unreadCount = allMessages.count { it.receiverId == currentUserId && !it.isRead }
                        Log.d("MessagingViewModel", "Updated unread messages count (sent): $unreadCount")
                        _unreadMessagesCount.value = unreadCount
                    }
                }

            receivedMessagesListener = firestore.collection("messages")
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MessagingViewModel", "Error listening to received messages: ${error.message}", error)
                        _errorState.value = "Error listening to received messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("MessagingViewModel", "Received messages snapshot is null")
                        return@addSnapshotListener
                    }

                    viewModelScope.launch {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            val message = doc.toObject<Message>()
                            message?.copy(
                                id = doc.id,
                                senderId = doc.getString("senderId") ?: "",
                                receiverId = doc.getString("receiverId") ?: "",
                                content = doc.getString("content") ?: "",
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                                isRead = doc.getBoolean("read") ?: false // Cambiado a "read"
                            )
                        }

                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex = currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedByDescending { it.timestamp.toDate().time }

                        Log.d("MessagingViewModel", "Received messages snapshot received, ${allMessages.size} messages: $allMessages")

                        val conversationMap = mutableMapOf<String, MutableList<Message>>()
                        allMessages.forEach { message ->
                            val otherUserId = if (message.senderId == currentUserId) message.receiverId else message.senderId
                            val conversationId = if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
                            val conversationMessages = conversationMap.getOrPut(conversationId) { mutableListOf() }
                            conversationMessages.add(message)
                        }

                        val conversations = conversationMap.map { (conversationId, messages) ->
                            val otherUserId = messages.first().let {
                                if (it.senderId == currentUserId) it.receiverId else it.senderId
                            }
                            val userSnapshot = firestore.collection("users").document(otherUserId).get().await()
                            val user = userSnapshot.toObject<User>()
                            val lastMessage = messages.maxByOrNull { it.timestamp }!!
                            Conversation(
                                userId = otherUserId,
                                username = user?.name ?: user?.username ?: "Unknown",
                                handle = "@${user?.username ?: "unknown"}",
                                lastMessage = lastMessage.content,
                                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(lastMessage.timestamp.toDate()),
                                profileImageUrl = user?.photoUrl ?: ""
                            )
                        }.sortedByDescending { it.date }

                        Log.d("MessagingViewModel", "Updated ${conversations.size} conversations: $conversations")
                        _conversations.value = conversations

                        val unreadCount = allMessages.count { it.receiverId == currentUserId && !it.isRead }
                        Log.d("MessagingViewModel", "Updated unread messages count (received): $unreadCount")
                        _unreadMessagesCount.value = unreadCount
                    }
                }
        }
    }

    fun setupMessagesListenerForChat(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            Log.d("MessagingViewModel", "Setting up messages listener for chat between $currentUserId and $targetUserId")

            val sentChatMessagesListener = firestore.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", targetUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MessagingViewModel", "Error listening to sent chat messages: ${error.message}", error)
                        _errorState.value = "Error listening to sent chat messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("MessagingViewModel", "Sent chat messages snapshot is null")
                        return@addSnapshotListener
                    }

                    viewModelScope.launch {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            val message = doc.toObject<Message>()
                            message?.copy(
                                id = doc.id,
                                senderId = doc.getString("senderId") ?: "",
                                receiverId = doc.getString("receiverId") ?: "",
                                content = doc.getString("content") ?: "",
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                                isRead = doc.getBoolean("read") ?: false // Cambiado a "read"
                            )
                        }

                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex = currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedBy { it.timestamp.toDate().time }

                        Log.d("MessagingViewModel", "Chat snapshot received (sent), ${allMessages.size} messages: $allMessages")
                        _messages.value = allMessages

                        val messagesToMarkAsRead = allMessages.filter { it.receiverId == currentUserId && !it.isRead }
                        Log.d("MessagingViewModel", "Found ${messagesToMarkAsRead.size} messages to mark as read")
                        messagesToMarkAsRead.forEach { message ->
                            try {
                                firestore.collection("messages")
                                    .document(message.id)
                                    .update("read", true) // Cambiado a "read"
                                    .await()
                                Log.d("MessagingViewModel", "Marked chat message as read: ${message.id}")
                            } catch (e: Exception) {
                                Log.e("MessagingViewModel", "Error marking chat message as read: ${e.message}", e)
                                _errorState.value = "Error marking chat message as read: ${e.message}"
                            }
                        }
                    }
                }

            val receivedChatMessagesListener = firestore.collection("messages")
                .whereEqualTo("senderId", targetUserId)
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("MessagingViewModel", "Error listening to received chat messages: ${error.message}", error)
                        _errorState.value = "Error listening to received chat messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("MessagingViewModel", "Received chat messages snapshot is null")
                        return@addSnapshotListener
                    }

                    viewModelScope.launch {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            val message = doc.toObject<Message>()
                            message?.copy(
                                id = doc.id,
                                senderId = doc.getString("senderId") ?: "",
                                receiverId = doc.getString("receiverId") ?: "",
                                content = doc.getString("content") ?: "",
                                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                                isRead = doc.getBoolean("read") ?: false // Cambiado a "read"
                            )
                        }

                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex = currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedBy { it.timestamp.toDate().time }

                        Log.d("MessagingViewModel", "Chat snapshot received (received), ${allMessages.size} messages: $allMessages")
                        _messages.value = allMessages

                        val messagesToMarkAsRead = allMessages.filter { it.receiverId == currentUserId && !it.isRead }
                        Log.d("MessagingViewModel", "Found ${messagesToMarkAsRead.size} messages to mark as read")
                        messagesToMarkAsRead.forEach { message ->
                            try {
                                firestore.collection("messages")
                                    .document(message.id)
                                    .update("read", true) // Cambiado a "read"
                                    .await()
                                Log.d("MessagingViewModel", "Marked chat message as read: ${message.id}")
                            } catch (e: Exception) {
                                Log.e("MessagingViewModel", "Error marking chat message as read: ${e.message}", e)
                                _errorState.value = "Error marking chat message as read: ${e.message}"
                            }
                        }
                    }
                }

            chatMessagesListener?.remove()
            chatMessagesListener = sentChatMessagesListener
        }
    }

    override fun onCleared() {
        super.onCleared()
        sentMessagesListener?.remove()
        receivedMessagesListener?.remove()
        chatMessagesListener?.remove()
        Log.d("MessagingViewModel", "Listeners removed on ViewModel cleared")
    }
}