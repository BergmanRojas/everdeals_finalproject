package project.mobile.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import project.mobile.model.ForumTopic

class ForumViewModel(private val authManager: AuthManager) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _forumTopics = MutableStateFlow<List<ForumTopic>>(emptyList())
    val forumTopics: StateFlow<List<ForumTopic>> = _forumTopics

    init {
        loadForumTopics()
    }

    private fun loadForumTopics() {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("forum_topics")
                    .get()
                    .await()
                val topics = snapshot.documents.mapNotNull { doc ->
                    ForumTopic(
                        id = doc.id,
                        title = doc.getString("title") ?: "No title",
                        description = doc.getString("description") ?: "No description", // Añadido
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Anonymous",
                        postCount = doc.getLong("postCount")?.toInt() ?: 0
                    )
                }
                _forumTopics.value = topics
            } catch (e: Exception) {
                // Manejar error (puedes agregar un log o un estado de error si lo deseas)
            }
        }
    }
}