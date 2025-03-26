package project.mobile.model

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ForumRepository {
    private val db = FirebaseFirestore.getInstance()

    fun getForumTopics() = flow {
        val snapshot = db.collection("forum_topics").get().await()
        val topics = snapshot.documents.mapNotNull { it.toObject(ForumTopic::class.java) }
        emit(topics)
    }

    suspend fun addForumTopic(topic: ForumTopic) {
        db.collection("forum_topics").document(topic.id).set(topic).await()
    }
}