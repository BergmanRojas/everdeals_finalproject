package project.mobile.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import project.mobile.model.Message
import project.mobile.model.Product
import project.mobile.model.User
import project.mobile.model.UserActivity
import project.mobile.utils.getCurrentUserId

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

    private val _forumTopics = MutableStateFlow<List<String>>(emptyList())
    val forumTopics: StateFlow<List<String>> = _forumTopics

    private val _stats = MutableStateFlow<List<String>>(emptyList())
    val stats: StateFlow<List<String>> = _stats

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // Nuevos estados para followers y following con nombres
    private val _followersWithNames = MutableStateFlow<List<Pair<String, String>>>(emptyList()) // (id, name)
    val followersWithNames: StateFlow<List<Pair<String, String>>> = _followersWithNames

    private val _followingWithNames = MutableStateFlow<List<Pair<String, String>>>(emptyList()) // (id, name)
    val followingWithNames: StateFlow<List<Pair<String, String>>> = _followingWithNames

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = authManager.getCurrentUser()
            if (user != null) {
                _userState.value = user
                loadProfileData(user.id)
            } else {
                Log.e("ProfileViewModel", "No user found")
                _fetchError.value = "User not authenticated"
                _isLoading.value = false
            }
        }
    }

    fun loadProfileData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _fetchError.value = null

            try {
                Log.d("ProfileViewModel", "Loading profile data for userId: $userId")

                // Cargar datos del usuario desde Firestore
                val userSnapshot = firestore.collection("users").document(userId).get().await()
                val user = userSnapshot.toObject(User::class.java)?.copy(id = userId)
                _userState.value = user

                // Activities
                val activitySnapshot = firestore.collection("user_interactions")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val comments = activitySnapshot.documents
                    .filter { it.getString("type") == "comment" }
                    .map { doc ->
                        UserActivity(
                            type = "comment",
                            content = doc.getString("comment") ?: "No comment",
                            timestamp = doc.getTimestamp("createdAt")
                        )
                    }

                val productSnapshot = firestore.collection("products")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val sharedProducts = productSnapshot.documents.map { doc ->
                    UserActivity(
                        type = "product_shared",
                        content = "Shared product: ${doc.getString("name") ?: "No name"}",
                        timestamp = doc.getTimestamp("createdAt")
                    )
                }
                val likesDislikes = activitySnapshot.documents
                    .filter { it.getString("type") in listOf("like", "dislike") }
                    .map { doc ->
                        UserActivity(
                            type = doc.getString("type") ?: "unknown",
                            content = "${doc.getString("type")?.capitalize()} on product: ${doc.getString("productName") ?: "Unknown"}",
                            timestamp = doc.getTimestamp("createdAt")
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
                val forumSnapshot = firestore.collection("forum_topics")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                _forumTopics.value = forumSnapshot.documents.map { it.getString("title") ?: "No title" }

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
                val followersIds = user?.followers ?: emptyList()
                val followingIds = user?.following ?: emptyList()

                val followersList = followersIds.map { id ->
                    val followerSnapshot = firestore.collection("users").document(id).get().await()
                    val follower = followerSnapshot.toObject(User::class.java)
                    Pair(id, follower?.name ?: follower?.username ?: "Unknown")
                }
                _followersWithNames.value = followersList

                val followingList = followingIds.map { id ->
                    val followingSnapshot = firestore.collection("users").document(id).get().await()
                    val following = followingSnapshot.toObject(User::class.java)
                    Pair(id, following?.name ?: following?.username ?: "Unknown")
                }
                _followingWithNames.value = followingList

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error fetching data: ${e.message}", e)
                _fetchError.value = "Error loading profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
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
                    val dislikedBy = productSnapshot.get("dislikedBy") as? List<String> ?: emptyList()
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

                    transaction.update(productRef, mapOf(
                        "likes" to likes,
                        "dislikes" to dislikes,
                        "likedBy" to newLikedBy,
                        "dislikedBy" to newDislikedBy
                    ))
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

                    val currentFollowing = currentUserDoc.get("following") as? List<String> ?: emptyList()
                    val targetFollowers = targetUserDoc.get("followers") as? List<String> ?: emptyList()

                    if (currentFollowing.contains(targetUserId)) {
                        transaction.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))
                        transaction.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
                    } else {
                        transaction.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))
                        transaction.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
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

                // Recargar followers y following después de toggle
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
                timestamp = Timestamp.now()
            )
            try {
                firestore.collection("messages")
                    .document(message.id)
                    .set(message)
                    .await()
                _messages.value = _messages.value + message
            } catch (e: Exception) {
                _errorState.value = "Error sending message: ${e.message}"
            }
        }
    }

    fun loadMessagesWithUser(targetUserId: String) {
        viewModelScope.launch {
            val currentUserId = authManager.getCurrentUser()?.id ?: return@launch
            try {
                val messagesSnapshot = firestore.collection("messages")
                    .whereIn("senderId", listOf(currentUserId, targetUserId))
                    .whereIn("receiverId", listOf(currentUserId, targetUserId))
                    .orderBy("timestamp")
                    .get()
                    .await()
                _messages.value = messagesSnapshot.documents.map { doc ->
                    Message(
                        id = doc.getString("id") ?: "",
                        senderId = doc.getString("senderId") ?: "",
                        receiverId = doc.getString("receiverId") ?: "",
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now()
                    )
                }
            } catch (e: Exception) {
                _errorState.value = "Error loading messages: ${e.message}"
            }
        }
    }
}

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Timestamp = Timestamp.now()
)