package project.mobile.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import project.mobile.model.*
import project.mobile.utils.getCurrentUserId
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileViewModel(
    private val authManager: AuthManager
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private val _activities = MutableStateFlow<List<UserActivity>>(emptyList())
    val activities: StateFlow<List<UserActivity>> = _activities

    private val _deals = MutableStateFlow<List<Product>>(emptyList())
    val deals: StateFlow<List<Product>> = _deals

    private val _forumTopics = MutableStateFlow<List<ForumTopic>>(emptyList())
    val forumTopics: StateFlow<List<ForumTopic>> = _forumTopics

    private val _stats = MutableStateFlow<List<String>>(emptyList())
    val stats: StateFlow<List<String>> = _stats

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _unreadMessagesCount = MutableStateFlow(0)
    val unreadMessagesCount: StateFlow<Int> = _unreadMessagesCount

    private val _followersWithNames = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val followersWithNames: StateFlow<List<Pair<String, String>>> = _followersWithNames

    private val _followingWithNames = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val followingWithNames: StateFlow<List<Pair<String, String>>> = _followingWithNames

    // Propiedades para almacenar los listeners
    private var sentMessagesListener: ListenerRegistration? = null
    private var receivedMessagesListener: ListenerRegistration? = null
    private var chatMessagesListener: ListenerRegistration? = null

    fun loadProfileData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _fetchError.value = null
            Log.d("ProfileViewModel", "Starting to load profile data for userId: $userId")

            val result = withTimeoutOrNull(10000L) {
                try {
                    Log.d("ProfileViewModel", "Fetching user from server for userId: $userId")
                    val userSnapshot = try {
                        firestore.collection("users").document(userId).get(Source.SERVER).await()
                    } catch (e: FirebaseFirestoreException) {
                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w("ProfileViewModel", "Permission denied for userId: $userId")
                            _fetchError.value = "Permission denied to access profile"
                            return@withTimeoutOrNull
                        } else if (e.message?.contains("offline") == true) {
                            Log.w(
                                "ProfileViewModel",
                                "Client offline, attempting cache for userId: $userId"
                            )
                            firestore.collection("users").document(userId).get(Source.CACHE).await()
                        } else {
                            throw e
                        }
                    }

                    if (!userSnapshot.exists()) {
                        Log.w(
                            "ProfileViewModel",
                            "User document does not exist for userId: $userId"
                        )
                        _fetchError.value = "Profile not found"
                        return@withTimeoutOrNull
                    }

                    val user = userSnapshot.toObject<User>()?.copy(id = userId)
                    _userState.value = user

                    if (user == null) {
                        Log.w("ProfileViewModel", "Failed to deserialize user for userId: $userId")
                        _fetchError.value = "Profile data invalid"
                        return@withTimeoutOrNull
                    }

                    Log.d("ProfileViewModel", "User loaded: ${user.name} (${user.id})")

                    // Cargar actividades
                    val activitySnapshot = try {
                        Log.d(
                            "ProfileViewModel",
                            "Fetching activities from server for userId: $userId"
                        )
                        firestore.collection("user_interactions")
                            .whereEqualTo("userId", userId)
                            .get(Source.SERVER)
                            .await()
                    } catch (e: FirebaseFirestoreException) {
                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w(
                                "ProfileViewModel",
                                "Permission denied for activities of userId: $userId"
                            )
                            _fetchError.value = "Permission denied to access activities"
                            return@withTimeoutOrNull
                        } else if (e.message?.contains("offline") == true) {
                            Log.w(
                                "ProfileViewModel",
                                "Offline, loading activities from cache for userId: $userId"
                            )
                            firestore.collection("user_interactions")
                                .whereEqualTo("userId", userId)
                                .get(Source.CACHE)
                                .await()
                        } else {
                            throw e
                        }
                    }

                    val comments = activitySnapshot.documents
                        .filter { it.getString("type") == "comment" }
                        .map { doc ->
                            UserActivity(
                                type = "comment",
                                content = doc.getString("comment") ?: "No comment",
                                timestamp = doc.getTimestamp("createdAt"),
                                userId = doc.getString("userId") ?: ""
                            )
                        }

                    // Cargar productos (deals)
                    val productSnapshot = try {
                        Log.d(
                            "ProfileViewModel",
                            "Fetching products from server for userId: $userId"
                        )
                        firestore.collection("products")
                            .whereEqualTo("userId", userId)
                            .get(Source.SERVER)
                            .await()
                    } catch (e: FirebaseFirestoreException) {
                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w(
                                "ProfileViewModel",
                                "Permission denied for products of userId: $userId"
                            )
                            _fetchError.value = "Permission denied to access products"
                            return@withTimeoutOrNull
                        } else if (e.message?.contains("offline") == true) {
                            Log.w(
                                "ProfileViewModel",
                                "Offline, loading products from cache for userId: $userId"
                            )
                            firestore.collection("products")
                                .whereEqualTo("userId", userId)
                                .get(Source.CACHE)
                                .await()
                        } else {
                            throw e
                        }
                    }

                    val sharedProducts = productSnapshot.documents.map { doc ->
                        UserActivity(
                            type = "product_shared",
                            content = "Shared product: ${doc.getString("name") ?: "No name"}",
                            timestamp = doc.getTimestamp("createdAt"),
                            userId = doc.getString("userId") ?: ""
                        )
                    }

                    val likesDislikes = activitySnapshot.documents
                        .filter { it.getString("type") in listOf("like", "dislike") }
                        .map { doc ->
                            UserActivity(
                                type = doc.getString("type") ?: "unknown",
                                content = "${
                                    doc.getString("type")?.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                                    } ?: "Unknown"
                                } on product: ${doc.getString("productName") ?: "Unknown"}",
                                timestamp = doc.getTimestamp("createdAt"),
                                userId = doc.getString("userId") ?: ""
                            )
                        }

                    _activities.value = (comments + sharedProducts + likesDislikes)
                        .sortedByDescending { it.timestamp?.toDate()?.time }

                    // Deals
                    _deals.value = productSnapshot.documents.map { doc ->
                        Product(
                            id = doc.getString("id") ?: "",
                            name = doc.getString("name") ?: "No name",
                            description = doc.getString("description") ?: "",
                            currentPrice = doc.getDouble("currentPrice") ?: 0.0,
                            originalPrice = doc.getDouble("originalPrice") ?: 0.0,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            amazonUrl = doc.getString("amazonUrl") ?: "",
                            category = doc.getString("category") ?: "",
                            startDate = doc.getString("startDate") ?: "",
                            endDate = doc.getString("endDate") ?: "",
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "",
                            likes = (doc.getLong("likes") ?: 0L).toInt(),
                            dislikes = (doc.getLong("dislikes") ?: 0L).toInt(),
                            likedBy = doc.get("likedBy") as? List<String> ?: emptyList(),
                            dislikedBy = doc.get("dislikedBy") as? List<String> ?: emptyList(),
                            createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
                        )
                    }

                    // Forum topics
                    val forumSnapshot = try {
                        Log.d(
                            "ProfileViewModel",
                            "Fetching forum topics from server for userId: $userId"
                        )
                        firestore.collection("forum_topics")
                            .whereEqualTo("userId", userId)
                            .get(Source.SERVER)
                            .await()
                    } catch (e: FirebaseFirestoreException) {
                        if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Log.w(
                                "ProfileViewModel",
                                "Permission denied for forum topics of userId: $userId"
                            )
                            _fetchError.value = "Permission denied to access forum topics"
                            return@withTimeoutOrNull
                        } else if (e.message?.contains("offline") == true) {
                            Log.w(
                                "ProfileViewModel",
                                "Offline, loading forum topics from cache for userId: $userId"
                            )
                            firestore.collection("forum_topics")
                                .whereEqualTo("userId", userId)
                                .get(Source.CACHE)
                                .await()
                        } else {
                            throw e
                        }
                    }
                    _forumTopics.value =
                        forumSnapshot.documents.mapNotNull { doc -> doc.toObject<ForumTopic>() }

                    // Statistics
                    val totalProducts = _deals.value.size
                    val totalLikesReceived = _deals.value.sumOf { it.likes }
                    val totalComments = comments.size
                    _stats.value = listOf(
                        "Products shared: $totalProducts",
                        "Likes received: $totalLikesReceived",
                        "Comments made: $totalComments"
                    )

                    // Cargar nombres de followers y following
                    val followersIds: List<String> = user.followers
                    val followingIds: List<String> = user.following

                    val followersList = mutableListOf<Pair<String, String>>()
                    for (id in followersIds) {
                        try {
                            val followerSnapshot =
                                firestore.collection("users").document(id).get(Source.SERVER)
                                    .await()
                            val follower = followerSnapshot.toObject<User>()
                            followersList.add(
                                Pair(
                                    id,
                                    follower?.name ?: follower?.username ?: "Unknown"
                                )
                            )
                            Log.d(
                                "ProfileViewModel",
                                "Loaded follower from server: ${follower?.name} ($id)"
                            )
                        } catch (e: FirebaseFirestoreException) {
                            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                Log.w("ProfileViewModel", "Permission denied for follower $id")
                                followersList.add(Pair(id, "Unknown (access denied)"))
                            } else if (e.message?.contains("offline") == true) {
                                val cachedSnapshot =
                                    firestore.collection("users").document(id).get(Source.CACHE)
                                        .await()
                                val follower = cachedSnapshot.toObject<User>()
                                followersList.add(
                                    Pair(
                                        id,
                                        follower?.name ?: follower?.username ?: "Unknown (cached)"
                                    )
                                )
                                Log.d(
                                    "ProfileViewModel",
                                    "Loaded follower from cache: ${follower?.name} ($id)"
                                )
                            } else {
                                Log.w(
                                    "ProfileViewModel",
                                    "Error loading follower $id: ${e.message}"
                                )
                                followersList.add(Pair(id, "Unknown (error)"))
                            }
                        }
                    }
                    _followersWithNames.value = followersList

                    val followingList = mutableListOf<Pair<String, String>>()
                    for (id in followingIds) {
                        try {
                            val followingSnapshot =
                                firestore.collection("users").document(id).get(Source.SERVER)
                                    .await()
                            val following = followingSnapshot.toObject<User>()
                            followingList.add(
                                Pair(
                                    id,
                                    following?.name ?: following?.username ?: "Unknown"
                                )
                            )
                            Log.d(
                                "ProfileViewModel",
                                "Loaded following from server: ${following?.name} ($id)"
                            )
                        } catch (e: FirebaseFirestoreException) {
                            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                Log.w("ProfileViewModel", "Permission denied for following $id")
                                followingList.add(Pair(id, "Unknown (access denied)"))
                            } else if (e.message?.contains("offline") == true) {
                                val cachedSnapshot =
                                    firestore.collection("users").document(id).get(Source.CACHE)
                                        .await()
                                val following = cachedSnapshot.toObject<User>()
                                followingList.add(
                                    Pair(
                                        id,
                                        following?.name ?: following?.username ?: "Unknown (cached)"
                                    )
                                )
                                Log.d(
                                    "ProfileViewModel",
                                    "Loaded following from cache: ${following?.name} ($id)"
                                )
                            } else {
                                Log.w(
                                    "ProfileViewModel",
                                    "Error loading following $id: ${e.message}"
                                )
                                followingList.add(Pair(id, "Unknown (error)"))
                            }
                        }
                    }
                    _followingWithNames.value = followingList

                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error fetching profile data: ${e.message}", e)
                    _fetchError.value = "Error loading profile: ${e.message}"
                }
            }

            if (result == null) {
                Log.w("ProfileViewModel", "Profile load timed out for userId: $userId")
                _fetchError.value = "Profile load timed out"
            }

            Log.d("ProfileViewModel", "Profile load completed for userId: $userId")
            _isLoading.value = false
        }
    }

    fun updateProfilePhoto(photoUrl: String) {
        viewModelScope.launch {
            try {
                val userId = _userState.value?.id ?: return@launch
                firestore.collection("users").document(userId)
                    .update("photoUrl", photoUrl)
                    .await()
                _userState.value = _userState.value?.copy(photoUrl = photoUrl)
            } catch (e: Exception) {
                _errorState.value = "Error updating profile photo: ${e.message}"
            }
        }
    }

    fun updateBackgroundPhoto(backgroundUrl: String) {
        viewModelScope.launch {
            try {
                val userId = _userState.value?.id ?: return@launch
                firestore.collection("users").document(userId)
                    .update("backgroundUrl", backgroundUrl)
                    .await()
                _userState.value = _userState.value?.copy(backgroundUrl = backgroundUrl)
            } catch (e: Exception) {
                _errorState.value = "Error updating background photo: ${e.message}"
            }
        }
    }

    fun toggleLikeDislike(productId: String, isLike: Boolean) {
        viewModelScope.launch {
            try {
                val userId = getCurrentUserId()
                val productRef = firestore.collection("products").document(productId)
                firestore.runTransaction { transaction ->
                    val productSnapshot = transaction.get(productRef)
                    val likedBy = productSnapshot.get("likedBy") as? List<String> ?: emptyList()
                    val dislikedBy =
                        productSnapshot.get("dislikedBy") as? List<String> ?: emptyList()
                    var likes = productSnapshot.getLong("likes")?.toInt() ?: 0
                    var dislikes = productSnapshot.getLong("dislikes")?.toInt() ?: 0

                    val newLikedBy = likedBy.toMutableList()
                    val newDislikedBy = dislikedBy.toMutableList()

                    if (isLike) {
                        if (newLikedBy.contains(userId)) {
                            newLikedBy.remove(userId)
                            likes--
                        } else {
                            newLikedBy.add(userId)
                            likes++
                            if (newDislikedBy.contains(userId)) {
                                newDislikedBy.remove(userId)
                                dislikes--
                            }
                        }
                    } else {
                        if (newDislikedBy.contains(userId)) {
                            newDislikedBy.remove(userId)
                            dislikes--
                        } else {
                            newDislikedBy.add(userId)
                            dislikes++
                            if (newLikedBy.contains(userId)) {
                                newLikedBy.remove(userId)
                                likes--
                            }
                        }
                    }

                    transaction.update(
                        productRef, mapOf(
                            "likes" to likes,
                            "dislikes" to dislikes,
                            "likedBy" to newLikedBy,
                            "dislikedBy" to newDislikedBy
                        )
                    )
                }.await()

                _deals.value = _deals.value.map { product ->
                    if (product.id == productId) {
                        product.copy(
                            likes = if (isLike && !product.likedBy.contains(userId)) product.likes + 1 else if (isLike) product.likes - 1 else product.likes,
                            dislikes = if (!isLike && !product.dislikedBy.contains(userId)) product.dislikes + 1 else if (!isLike) product.dislikes - 1 else product.dislikes,
                            likedBy = if (isLike && product.likedBy.contains(userId)) product.likedBy - userId else if (isLike) product.likedBy + userId else product.likedBy,
                            dislikedBy = if (!isLike && product.dislikedBy.contains(userId)) product.dislikedBy - userId else if (!isLike) product.dislikedBy + userId else product.dislikedBy
                        )
                    } else product
                }
            } catch (e: Exception) {
                _errorState.value = "Error toggling like/dislike: ${e.message}"
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            val currentUser = authManager.getCurrentUser() ?: return@launch
            val currentUserId = currentUser.id
            try {
                val currentUserRef = firestore.collection("users").document(currentUserId)
                val targetUserRef = firestore.collection("users").document(targetUserId)

                firestore.runTransaction { transaction ->
                    val currentUserDoc = transaction.get(currentUserRef)
                    val targetUserDoc = transaction.get(targetUserRef)

                    val currentFollowing =
                        currentUserDoc.get("following") as? List<String> ?: emptyList()
                    val targetFollowers =
                        targetUserDoc.get("followers") as? List<String> ?: emptyList()

                    if (currentFollowing.contains(targetUserId)) {
                        transaction.update(
                            currentUserRef,
                            "following",
                            FieldValue.arrayRemove(targetUserId)
                        )
                        transaction.update(
                            targetUserRef,
                            "followers",
                            FieldValue.arrayRemove(currentUserId)
                        )
                    } else {
                        transaction.update(
                            currentUserRef,
                            "following",
                            FieldValue.arrayUnion(targetUserId)
                        )
                        transaction.update(
                            targetUserRef,
                            "followers",
                            FieldValue.arrayUnion(currentUserId)
                        )
                    }
                }.await()

                _userState.value = _userState.value?.let {
                    val newFollowing = if (it.following.contains(targetUserId)) {
                        it.following - targetUserId
                    } else {
                        it.following + targetUserId
                    }
                    it.copy(following = newFollowing)
                }

                loadProfileData(userId = _userState.value?.id ?: return@launch)
            } catch (e: Exception) {
                _errorState.value = "Error toggling follow: ${e.message}"
            }
        }
    }

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
                Log.d("ProfileViewModel", "Attempting to send message: $message")
                firestore.collection("messages")
                    .document(message.id)
                    .set(message)
                    .await()
                Log.d("ProfileViewModel", "Message sent successfully: $message")
                _messages.value = _messages.value + message
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error sending message: ${e.message}", e)
                _errorState.value = "Error sending message: ${e.message}"
            }
        }
    }

    fun loadMessagesWithUser(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            try {
                Log.d(
                    "ProfileViewModel",
                    "Loading messages between $currentUserId and $targetUserId"
                )
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
                        isRead = doc.getBoolean("isRead") ?: false
                    )
                }
                Log.d("ProfileViewModel", "Loaded ${messages.size} messages: $messages")
                _messages.value = messages

                // Mark messages as read
                messages.filter { it.receiverId == currentUserId && !it.isRead }
                    .forEach { message ->
                        firestore.collection("messages")
                            .document(message.id)
                            .update("isRead", true)
                            .await()
                        Log.d("ProfileViewModel", "Marked message as read: ${message.id}")
                    }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading messages: ${e.message}", e)
                _errorState.value = "Error loading messages: ${e.message}"
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            try {
                Log.d("ProfileViewModel", "Loading conversations for user: $currentUserId")

                // Consulta para mensajes enviados por el usuario actual
                val sentMessagesSnapshot = firestore.collection("messages")
                    .whereEqualTo("senderId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                // Consulta para mensajes recibidos por el usuario actual
                val receivedMessagesSnapshot = firestore.collection("messages")
                    .whereEqualTo("receiverId", currentUserId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                // Combinar los mensajes
                val messages = (sentMessagesSnapshot.documents + receivedMessagesSnapshot.documents)
                    .mapNotNull { doc ->
                        val message = doc.toObject<Message>()
                        message?.copy(
                            id = doc.id,
                            senderId = doc.getString("senderId") ?: "",
                            receiverId = doc.getString("receiverId") ?: "",
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                            isRead = doc.getBoolean("isRead") ?: false
                        )
                    }
                    .distinctBy { it.id } // Evitar duplicados
                    .sortedByDescending { it.timestamp.toDate().time }

                Log.d(
                    "ProfileViewModel",
                    "Loaded ${messages.size} messages for conversations: $messages"
                )

                // Agrupar mensajes por conversación (par de usuarios)
                val conversationMap = mutableMapOf<String, MutableList<Message>>()
                messages.forEach { message ->
                    val otherUserId =
                        if (message.senderId == currentUserId) message.receiverId else message.senderId
                    val conversationId =
                        if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
                    val conversationMessages =
                        conversationMap.getOrPut(conversationId) { mutableListOf() }
                    conversationMessages.add(message)
                }

                // Convertir los mensajes agrupados en conversaciones
                val conversations = conversationMap.map { (conversationId, messages) ->
                    val otherUserId = messages.first().let {
                        if (it.senderId == currentUserId) it.receiverId else it.senderId
                    }
                    val userSnapshot =
                        firestore.collection("users").document(otherUserId).get().await()
                    val user = userSnapshot.toObject<User>()
                    val lastMessage = messages.maxByOrNull { it.timestamp }!!
                    Conversation(
                        userId = otherUserId,
                        username = user?.name ?: user?.username ?: "Unknown",
                        handle = "@${user?.username ?: "unknown"}",
                        lastMessage = lastMessage.content,
                        date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                            lastMessage.timestamp.toDate()
                        ),
                        profileImageUrl = user?.photoUrl ?: ""
                    )
                }.sortedByDescending { it.date }

                Log.d(
                    "ProfileViewModel",
                    "Loaded ${conversations.size} conversations: $conversations"
                )
                _conversations.value = conversations

                // Contar mensajes no leídos
                val unreadCount = messages.count { it.receiverId == currentUserId && !it.isRead }
                Log.d("ProfileViewModel", "Unread messages count: $unreadCount")
                _unreadMessagesCount.value = unreadCount
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading conversations: ${e.message}", e)
                _errorState.value = "Error loading conversations: ${e.message}"
            }
        }
    }

    fun setupMessagesListener() {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            Log.d("ProfileViewModel", "Setting up messages listener for user: $currentUserId")

            // Listener para mensajes enviados
            sentMessagesListener = firestore.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            "ProfileViewModel",
                            "Error listening to sent messages: ${error.message}",
                            error
                        )
                        _errorState.value = "Error listening to sent messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("ProfileViewModel", "Sent messages snapshot is null")
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
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }

                        // Combinar con los mensajes existentes para evitar duplicados
                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex =
                                currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedByDescending { it.timestamp.toDate().time }

                        Log.d(
                            "ProfileViewModel",
                            "Snapshot received, ${allMessages.size} messages: $allMessages"
                        )

                        // Agrupar mensajes por conversación
                        val conversationMap = mutableMapOf<String, MutableList<Message>>()
                        allMessages.forEach { message ->
                            val otherUserId =
                                if (message.senderId == currentUserId) message.receiverId else message.senderId
                            val conversationId =
                                if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
                            val conversationMessages =
                                conversationMap.getOrPut(conversationId) { mutableListOf() }
                            conversationMessages.add(message)
                        }

                        // Convertir los mensajes agrupados en conversaciones
                        val conversations = conversationMap.map { (conversationId, messages) ->
                            val otherUserId = messages.first().let {
                                if (it.senderId == currentUserId) it.receiverId else it.senderId
                            }
                            val userSnapshot =
                                firestore.collection("users").document(otherUserId).get().await()
                            val user = userSnapshot.toObject<User>()
                            val lastMessage = messages.maxByOrNull { it.timestamp }!!
                            Conversation(
                                userId = otherUserId,
                                username = user?.name ?: user?.username ?: "Unknown",
                                handle = "@${user?.username ?: "unknown"}",
                                lastMessage = lastMessage.content,
                                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                    lastMessage.timestamp.toDate()
                                ),
                                profileImageUrl = user?.photoUrl ?: ""
                            )
                        }.sortedByDescending { it.date }

                        Log.d(
                            "ProfileViewModel",
                            "Updated ${conversations.size} conversations: $conversations"
                        )
                        _conversations.value = conversations

                        // Actualizar el conteo de mensajes no leídos
                        val unreadCount =
                            allMessages.count { it.receiverId == currentUserId && !it.isRead }
                        Log.d("ProfileViewModel", "Updated unread messages count: $unreadCount")
                        _unreadMessagesCount.value = unreadCount
                    }
                }

            // Listener para mensajes recibidos
            receivedMessagesListener = firestore.collection("messages")
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            "ProfileViewModel",
                            "Error listening to received messages: ${error.message}",
                            error
                        )
                        _errorState.value = "Error listening to received messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("ProfileViewModel", "Received messages snapshot is null")
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
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }

                        // Combinar con los mensajes existentes para evitar duplicados
                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex =
                                currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedByDescending { it.timestamp.toDate().time }

                        Log.d(
                            "ProfileViewModel",
                            "Snapshot received, ${allMessages.size} messages: $allMessages"
                        )

                        // Agrupar mensajes por conversación
                        val conversationMap = mutableMapOf<String, MutableList<Message>>()
                        allMessages.forEach { message ->
                            val otherUserId =
                                if (message.senderId == currentUserId) message.receiverId else message.senderId
                            val conversationId =
                                if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
                            val conversationMessages =
                                conversationMap.getOrPut(conversationId) { mutableListOf() }
                            conversationMessages.add(message)
                        }

                        // Convertir los mensajes agrupados en conversaciones
                        val conversations = conversationMap.map { (conversationId, messages) ->
                            val otherUserId = messages.first().let {
                                if (it.senderId == currentUserId) it.receiverId else it.senderId
                            }
                            val userSnapshot =
                                firestore.collection("users").document(otherUserId).get().await()
                            val user = userSnapshot.toObject<User>()
                            val lastMessage = messages.maxByOrNull { it.timestamp }!!
                            Conversation(
                                userId = otherUserId,
                                username = user?.name ?: user?.username ?: "Unknown",
                                handle = "@${user?.username ?: "unknown"}",
                                lastMessage = lastMessage.content,
                                date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
                                    lastMessage.timestamp.toDate()
                                ),
                                profileImageUrl = user?.photoUrl ?: ""
                            )
                        }.sortedByDescending { it.date }

                        Log.d(
                            "ProfileViewModel",
                            "Updated ${conversations.size} conversations: $conversations"
                        )
                        _conversations.value = conversations

                        // Actualizar el conteo de mensajes no leídos
                        val unreadCount =
                            allMessages.count { it.receiverId == currentUserId && !it.isRead }
                        Log.d("ProfileViewModel", "Updated unread messages count: $unreadCount")
                        _unreadMessagesCount.value = unreadCount
                    }
                }
        }
    }

    fun setupMessagesListenerForChat(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            Log.d(
                "ProfileViewModel",
                "Setting up messages listener for chat between $currentUserId and $targetUserId"
            )

            // Listener para mensajes enviados por el usuario actual al targetUserId
            val sentChatMessagesListener = firestore.collection("messages")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", targetUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            "ProfileViewModel",
                            "Error listening to sent chat messages: ${error.message}",
                            error
                        )
                        _errorState.value =
                            "Error listening to sent chat messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("ProfileViewModel", "Sent chat messages snapshot is null")
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
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }

                        // Combinar con los mensajes existentes para evitar duplicados
                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex =
                                currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedBy { it.timestamp.toDate().time }

                        Log.d(
                            "ProfileViewModel",
                            "Chat snapshot received, ${allMessages.size} messages: $allMessages"
                        )
                        _messages.value = allMessages

                        // Mark messages as read
                        allMessages.filter { it.receiverId == currentUserId && !it.isRead }
                            .forEach { message ->
                                firestore.collection("messages")
                                    .document(message.id)
                                    .update("isRead", true)
                                    .await()
                                Log.d(
                                    "ProfileViewModel",
                                    "Marked chat message as read: ${message.id}"
                                )
                            }
                    }
                }

            // Listener para mensajes recibidos por el usuario actual desde targetUserId
            val receivedChatMessagesListener = firestore.collection("messages")
                .whereEqualTo("senderId", targetUserId)
                .whereEqualTo("receiverId", currentUserId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(
                            "ProfileViewModel",
                            "Error listening to received chat messages: ${error.message}",
                            error
                        )
                        _errorState.value =
                            "Error listening to received chat messages: ${error.message}"
                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        Log.w("ProfileViewModel", "Received chat messages snapshot is null")
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
                                isRead = doc.getBoolean("isRead") ?: false
                            )
                        }

                        // Combinar con los mensajes existentes para evitar duplicados
                        val currentMessages = _messages.value.toMutableList()
                        messages.forEach { newMessage ->
                            val existingIndex =
                                currentMessages.indexOfFirst { it.id == newMessage.id }
                            if (existingIndex != -1) {
                                currentMessages[existingIndex] = newMessage
                            } else {
                                currentMessages.add(newMessage)
                            }
                        }

                        val allMessages = currentMessages.distinctBy { it.id }
                            .sortedBy { it.timestamp.toDate().time }

                        Log.d(
                            "ProfileViewModel",
                            "Chat snapshot received, ${allMessages.size} messages: $allMessages"
                        )
                        _messages.value = allMessages

                        // Mark messages as read
                        allMessages.filter { it.receiverId == currentUserId && !it.isRead }
                            .forEach { message ->
                                firestore.collection("messages")
                                    .document(message.id)
                                    .update("isRead", true)
                                    .await()
                                Log.d(
                                    "ProfileViewModel",
                                    "Marked chat message as read: ${message.id}"
                                )
                            }
                    }
                }

            // Actualizar el listener almacenado
            chatMessagesListener?.remove()
            chatMessagesListener = sentChatMessagesListener
            // Nota: No almacenamos receivedChatMessagesListener como chatMessagesListener porque solo necesitamos un listener activo,
            // pero ambos (sent y received) se limpiarán en onCleared
        }
    }

    override fun onCleared() {
        super.onCleared()
        sentMessagesListener?.remove()
        receivedMessagesListener?.remove()
        chatMessagesListener?.remove()
        Log.d("ProfileViewModel", "Listeners removed on ViewModel cleared")
    }
}