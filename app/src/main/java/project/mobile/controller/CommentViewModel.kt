package project.mobile.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import project.mobile.models.Comment
import com.google.firebase.Timestamp

class CommentViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    fun loadComments(productId: String) {
        viewModelScope.launch {
            try {
                val snapshot = db.collection("comments")
                    .whereEqualTo("productId", productId)
                    .orderBy("time", Query.Direction.DESCENDING)
                    .get()
                    .await()

                _comments.value = snapshot.toObjects(Comment::class.java)
            } catch (e: Exception) {
                _comments.value = emptyList()
            }
        }
    }

    fun addComment(productId: String, userId: String, text: String) {
        val newComment = Comment(
            userId = userId,
            productId = productId,
            comment = text,
            time = Timestamp.now()
        )

        viewModelScope.launch {
            try {
                db.collection("comments").add(newComment).await()
                loadComments(productId) // Recargar comentarios tras añadir uno nuevo
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}