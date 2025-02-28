package project.mobile.controller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import project.mobile.models.Comment
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

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
                db.collection("comments").add(newComment)
                loadComments(productId) // Recargar comentarios tras añadir uno nuevo
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}

@Composable
fun CommentList(commentViewModel: CommentViewModel, productId: String) {
    val comments by commentViewModel.comments.collectAsState()

    LaunchedEffect(productId) {
        commentViewModel.loadComments(productId)
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(comments) { comment ->
            CommentItem(comment)
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = comment.userId, style = MaterialTheme.typography.bodyLarge)
        Text(text = comment.comment, style = MaterialTheme.typography.bodyMedium)
        Text(text = formatTimestamp(comment.time), style = MaterialTheme.typography.bodySmall)
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@Preview(showBackground = true)
@Composable
fun PreviewCommentList() {
    val fakeViewModel = CommentViewModel()
    CommentList(commentViewModel = fakeViewModel, productId = "sample_product_id")
}
