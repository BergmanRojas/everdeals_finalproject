package project.mobile.controller

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import project.mobile.model.User
import java.util.Locale

class ProfileViewModel(
    private val authManager: AuthManager
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _followersWithNames = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val followersWithNames: StateFlow<List<Pair<String, String>>> = _followersWithNames

    private val _followingWithNames = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val followingWithNames: StateFlow<List<Pair<String, String>>> = _followingWithNames

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
                            Log.w("ProfileViewModel", "Client offline, attempting cache for userId: $userId")
                            firestore.collection("users").document(userId).get(Source.CACHE).await()
                        } else {
                            throw e
                        }
                    }

                    if (!userSnapshot.exists()) {
                        Log.w("ProfileViewModel", "User document does not exist for userId: $userId")
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

                    // Cargar nombres de followers y following
                    val followersIds: List<String> = user.followers
                    val followingIds: List<String> = user.following

                    val followersList = mutableListOf<Pair<String, String>>()
                    for (id in followersIds) {
                        try {
                            val followerSnapshot = firestore.collection("users").document(id).get(Source.SERVER).await()
                            val follower = followerSnapshot.toObject<User>()
                            followersList.add(Pair(id, follower?.name ?: follower?.username ?: "Unknown"))
                            Log.d("ProfileViewModel", "Loaded follower from server: ${follower?.name} ($id)")
                        } catch (e: FirebaseFirestoreException) {
                            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                Log.w("ProfileViewModel", "Permission denied for follower $id")
                                followersList.add(Pair(id, "Unknown (access denied)"))
                            } else if (e.message?.contains("offline") == true) {
                                val cachedSnapshot = firestore.collection("users").document(id).get(Source.CACHE).await()
                                val follower = cachedSnapshot.toObject<User>()
                                followersList.add(Pair(id, follower?.name ?: follower?.username ?: "Unknown (cached)"))
                                Log.d("ProfileViewModel", "Loaded follower from cache: ${follower?.name} ($id)")
                            } else {
                                Log.w("ProfileViewModel", "Error loading follower $id: ${e.message}")
                                followersList.add(Pair(id, "Unknown (error)"))
                            }
                        }
                    }
                    _followersWithNames.value = followersList

                    val followingList = mutableListOf<Pair<String, String>>()
                    for (id in followingIds) {
                        try {
                            val followingSnapshot = firestore.collection("users").document(id).get(Source.SERVER).await()
                            val following = followingSnapshot.toObject<User>()
                            followingList.add(Pair(id, following?.name ?: following?.username ?: "Unknown"))
                            Log.d("ProfileViewModel", "Loaded following from server: ${following?.name} ($id)")
                        } catch (e: FirebaseFirestoreException) {
                            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                                Log.w("ProfileViewModel", "Permission denied for following $id")
                                followingList.add(Pair(id, "Unknown (access denied)"))
                            } else if (e.message?.contains("offline") == true) {
                                val cachedSnapshot = firestore.collection("users").document(id).get(Source.CACHE).await()
                                val following = cachedSnapshot.toObject<User>()
                                followingList.add(Pair(id, following?.name ?: following?.username ?: "Unknown (cached)"))
                                Log.d("ProfileViewModel", "Loaded following from cache: ${following?.name} ($id)")
                            } else {
                                Log.w("ProfileViewModel", "Error loading following $id: ${e.message}")
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

                loadProfileData(userId = _userState.value?.id ?: return@launch)
            } catch (e: Exception) {
                _errorState.value = "Error toggling follow: ${e.message}"
            }
        }
    }
}